package View;

import Utils.Util;
import Utils.ViewFieldMethodCreator;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.ui.components.JBScrollPane;
import entity.Element;
import entity.IdBean;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Created by wangzai on 2016/11/24.
 */
public class FindViewByIdDialog extends JFrame implements ActionListener, IdBean.EnableActionListener {
    private String mTitle = "FindViewByIdDialog";
    private Project mProject;
    private Editor mEditor;
    private String mSelectedText;
    private List<Element> mElements;
    // 获取当前文件
    private PsiFile mPsiFile;
    // 获取class
    private PsiClass mClass;


    // 标签JPanel
    private JPanel mPanelTitle = new JPanel();
    private JLabel mTitleId = new JLabel("ViewId");
    private JLabel mTitleClick = new JLabel("OnClick");
    private JLabel mTitleField = new JLabel("ViewFiled");

    // 内容JPanel
    private JPanel mContentJPanel = new JPanel();
    private GridBagLayout mContentLayout = new GridBagLayout();
    private GridBagConstraints mContentConstraints = new GridBagConstraints();
    // 内容JBScrollPane滚动
    private JBScrollPane jScrollPane;

    // 底部JPanel
    // LayoutInflater JPanel
    private JPanel mPanelInflater = new JPanel(new FlowLayout(FlowLayout.LEFT));
    // 是否全选
    private JCheckBox mCheckAll = new JCheckBox("ViewWidget");
    // 确定、取消JPanel
    private JPanel mPanelButtonRight = new JPanel();
    private JButton mButtonConfirm = new JButton("确定");
    private JButton mButtonCancel = new JButton("取消");

    // GridBagLayout不要求组件的大小相同便可以将组件垂直、水平或沿它们的基线对齐
    private GridBagLayout mLayout = new GridBagLayout();
    // GridBagConstraints用来控制添加进的组件的显示位置
    private GridBagConstraints mConstraints = new GridBagConstraints();

    public FindViewByIdDialog(Editor editor, Project project, PsiFile psiFile, PsiClass psiClass, List<Element> elements, String selectedText) {
        mEditor = editor;
        mProject = project;
        mSelectedText = selectedText;
        mElements = elements;
        mPsiFile = psiFile;
        mClass = psiClass;
        initTopPanel();
        initExist();
        initContentPanel();
        initBottomPanel();
        setConstraints();
        setDialog();
    }

    /**
     * 判断已存在的变量，设置全选
     * 判断onclick是否写入
     */
    private void initExist() {
        // 判断是否全选  记录当前可用的个数
        int mCurrentAbleSize = 0;
        // 判断是否已存在的变量
        boolean isFdExist = false;

        for (Element element : mElements) {
            // 判断ViewById是否存在
            if (mClass.getText().contains("@ViewById(" + element.getFullID() + ")")) {
                isFdExist = true;
            } else {
                isFdExist = false;
            }

            // 如果当前没有该属性注解存在
            if (!isFdExist) {
                mCurrentAbleSize++;
                element.setIsCreateFiled(true);
            } else {
                element.setIsCreateFiled(false);
            }

            mCheckAll.setSelected(mCurrentAbleSize == mElements.size());
            mCheckAll.addActionListener(this);
        }
    }

    /**
     * 添加头部
     */
    private void initTopPanel() {
        mPanelTitle.setLayout(new GridLayout(1, 4, 10, 10));
        mPanelTitle.setBorder(new EmptyBorder(5, 10, 5, 10));
        mTitleId.setHorizontalAlignment(JLabel.LEFT);
        mTitleClick.setHorizontalAlignment(JLabel.LEFT);
        mTitleField.setHorizontalAlignment(JLabel.LEFT);
        // 添加到JPanel
        mPanelTitle.add(mCheckAll);
        mPanelTitle.add(mTitleId);
        mPanelTitle.add(mTitleClick);
        mPanelTitle.add(mTitleField);
        mPanelTitle.setSize(720, 30);
        // 添加到JFrame
        getContentPane().add(mPanelTitle, 0);
    }

    /**
     * 添加底部
     */
    private void initBottomPanel() {
        // 添加监听
        mButtonConfirm.addActionListener(this);
        mButtonCancel.addActionListener(this);
        // 右边
        mPanelButtonRight.add(mButtonConfirm);
        mPanelButtonRight.add(mButtonCancel);
        // 添加到JFrame
        getContentPane().add(mPanelInflater, 2);
        getContentPane().add(mPanelButtonRight, 3);
    }

    /**
     * 解析mElements，并添加到JPanel
     */
    private void initContentPanel() {
        mContentJPanel.removeAll();
        // 设置内容
        for (int i = 0; i < mElements.size(); i++) {
            Element mElement = mElements.get(i);
            IdBean itemJPanel = new IdBean(new GridLayout(1, 4, 10, 10),
                    new EmptyBorder(5, 10, 5, 10),
                    new JCheckBox(mElement.getName()),
                    new JLabel(mElement.getId()),
                    new JCheckBox(),
                    new JTextField(mElement.getFieldName()),
                    mElement);
            // 监听
            itemJPanel.setEnableActionListener(this);
            itemJPanel.setClickActionListener(clickCheckBox -> mElement.setIsCreateClickMethod(clickCheckBox.isSelected()));
            itemJPanel.setFieldFocusListener(fieldJTextField -> mElement.setFieldName(fieldJTextField.getText()));
            mContentJPanel.add(itemJPanel);
            mContentConstraints.fill = GridBagConstraints.HORIZONTAL;
            mContentConstraints.gridwidth = 0;
            mContentConstraints.gridx = 0;
            mContentConstraints.gridy = i;
            mContentConstraints.weightx = 1;
            mContentLayout.setConstraints(itemJPanel, mContentConstraints);
        }
        mContentJPanel.setLayout(mContentLayout);
        jScrollPane = new JBScrollPane(mContentJPanel);
        jScrollPane.revalidate();
        // 添加到JFrame
        getContentPane().add(jScrollPane, 1);
    }

    /**
     * 设置Constraints
     */
    private void setConstraints() {
        // 使组件完全填满其显示区域
        mConstraints.fill = GridBagConstraints.BOTH;
        // 设置组件水平所占用的格子数，如果为0，就说明该组件是该行的最后一个
        mConstraints.gridwidth = 0;
        // 第几列
        mConstraints.gridx = 0;
        // 第几行
        mConstraints.gridy = 0;
        // 行拉伸0不拉伸，1完全拉伸
        mConstraints.weightx = 1;
        // 列拉伸0不拉伸，1完全拉伸
        mConstraints.weighty = 0;
        // 设置组件
        mLayout.setConstraints(mPanelTitle, mConstraints);
        mConstraints.fill = GridBagConstraints.BOTH;
        mConstraints.gridwidth = 1;
        mConstraints.gridx = 0;
        mConstraints.gridy = 1;
        mConstraints.weightx = 1;
        mConstraints.weighty = 1;
        mLayout.setConstraints(jScrollPane, mConstraints);
        mConstraints.fill = GridBagConstraints.HORIZONTAL;
        mConstraints.gridwidth = 0;
        mConstraints.gridx = 0;
        mConstraints.gridy = 2;
        mConstraints.weightx = 1;
        mConstraints.weighty = 0;
        mLayout.setConstraints(mPanelInflater, mConstraints);
        mConstraints.fill = GridBagConstraints.NONE;
        mConstraints.gridwidth = 0;
        mConstraints.gridx = 0;
        mConstraints.gridy = 3;
        mConstraints.weightx = 0;
        mConstraints.weighty = 0;
        mConstraints.anchor = GridBagConstraints.EAST;
        mLayout.setConstraints(mPanelButtonRight, mConstraints);
    }

    /**
     * 显示dialog
     */
    public void showDialog() {
        // 显示
        setVisible(true);
    }

    /**
     * 设置JFrame参数
     */
    private void setDialog() {
        // 设置标题
        setTitle(mTitle);
        // 设置布局管理
        setLayout(mLayout);
        // 不可拉伸
        setResizable(false);
        // 设置大小
        setSize(720, 405);
        // 自适应大小
        // pack();
        // 设置居中，放在setSize后面
        setLocationRelativeTo(null);
        // 显示最前
        setAlwaysOnTop(true);
    }

    /**
     * 关闭dialog
     */
    public void cancelDialog() {
        setVisible(false);
        dispose();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "确定":
                cancelDialog();
                setCreator();
                break;
            case "取消":
                cancelDialog();
                break;
            case "ViewWidget":
                // 刷新
                for (Element mElement : mElements) {
                    mElement.setIsCreateFiled(mCheckAll.isSelected());
                }
                remove(jScrollPane);
                initContentPanel();
                setConstraints();
                revalidate();
                break;
        }
    }

    /**
     * 生成
     */
    private void setCreator() {
        new ViewFieldMethodCreator(this, mEditor, mPsiFile, mClass,
                "Generate Injections", mElements, mSelectedText)
                .execute();
    }

    /**
     * 更新所有选中的CheckBox
     */
    private void updateAllSelectCb() {
        boolean isAllSelect = true;
        for (Element element : mElements) {
            if (!element.isCreateFiled()) {
                isAllSelect = false;
                break;
            }
        }
        mCheckAll.setSelected(isAllSelect);
    }

    @Override
    public void setEnable(JCheckBox enableCheckBox, Element element) {
        element.setIsCreateFiled(enableCheckBox.isSelected());
        updateAllSelectCb();
    }
}
