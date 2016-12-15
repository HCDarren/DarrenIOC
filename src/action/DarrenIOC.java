package action;

import Utils.Util;
import View.FindViewByIdDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.psi.xml.XmlFile;
import entity.Element;
import org.apache.http.util.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class DarrenIOC extends AnAction {
    private FindViewByIdDialog mDialog;
    private String mSelectedText;

    @Override
    public void actionPerformed(AnActionEvent e) {
        // 获取project
        Project project = e.getProject();
        // 获取选中内容
        final Editor mEditor = e.getData(PlatformDataKeys.EDITOR);
        if (null == mEditor) {
            return;
        }
        SelectionModel model = mEditor.getSelectionModel();
        mSelectedText = model.getSelectedText();
        // 未选中布局内容，显示dialog
        if (TextUtils.isEmpty(mSelectedText)) {
            // 获取光标所在位置的布局
            mSelectedText = getCurrentLayout(mEditor);
            if (TextUtils.isEmpty(mSelectedText)) {
                mSelectedText = Messages.showInputDialog(project, "布局内容：（不需要输入R.layout.）", "未选中布局内容，请输入layout文件名", Messages.getInformationIcon());
                if (TextUtils.isEmpty(mSelectedText)) {
                    Util.showPopupBalloon(mEditor, "未输入layout文件名", 5);
                    return;
                }
            }
        }
        // 获取布局文件，通过FilenameIndex.getFilesByName获取
        // GlobalSearchScope.allScope(project)搜索整个项目
        PsiFile[] psiFiles = FilenameIndex.getFilesByName(project, mSelectedText + ".xml", GlobalSearchScope.allScope(project));
        if (psiFiles.length <= 0) {
            Util.showPopupBalloon(mEditor, "未找到选中的布局文件" + mSelectedText, 5);
            return;
        }
        XmlFile xmlFile = (XmlFile) psiFiles[0];
        List<Element> elements = new ArrayList<>();
        Util.getIDsFromLayout(xmlFile, elements);
        // 将代码写入文件，不允许在主线程中进行实时的文件写入
        if (elements.size() != 0) {
            PsiFile psiFile = PsiUtilBase.getPsiFileInEditor(mEditor, project);
            PsiClass psiClass = Util.getTargetClass(mEditor, psiFile);
            // 有的话就创建变量和findViewById
            if (mDialog != null && mDialog.isShowing()) {
                mDialog.cancelDialog();
            }
            mDialog = new FindViewByIdDialog(mEditor, project, psiFile, psiClass, elements, mSelectedText);
            mDialog.showDialog();
        } else {
            Util.showPopupBalloon(mEditor, "未找到任何Id", 5);
        }
    }

    /**
     * 获取当前光标的layout文件
     */
    private String getCurrentLayout(Editor editor) {
        Document document = editor.getDocument();
        CaretModel caretModel = editor.getCaretModel();
        int caretOffset = caretModel.getOffset();
        int lineNum = document.getLineNumber(caretOffset);
        int lineStartOffset = document.getLineStartOffset(lineNum);
        int lineEndOffset = document.getLineEndOffset(lineNum);
        String lineContent = document.getText(new TextRange(lineStartOffset, lineEndOffset));
        String layoutMatching = "R.layout.";
        if (!TextUtils.isEmpty(lineContent) && lineContent.contains(layoutMatching)) {
            // 获取layout文件的字符串
            int startPosition = lineContent.indexOf(layoutMatching) + layoutMatching.length();
            int endPosition = lineContent.indexOf(")", startPosition);
            String layoutStr = lineContent.substring(startPosition, endPosition);
            // 可能是另外一种情况 View.inflate
            if (layoutStr.contains(",")) {
                endPosition = lineContent.indexOf(",", startPosition);
                layoutStr = lineContent.substring(startPosition, endPosition);
            }
            return layoutStr;
        }
        return null;
    }
}
