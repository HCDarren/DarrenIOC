package entity;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * Created by Darren on 2016/12/14.
 */
public class IdBean extends JPanel {
    private JCheckBox mEnableCheckBox;
    private JLabel mIdJLabel;
    private JCheckBox mClickCheckBox;
    private JTextField mFieldJTextField;

    /**
     * mEnableCheckBox接口
     */
    public interface EnableActionListener {
        void setEnable(JCheckBox enableCheckBox,Element element);
    }

    private EnableActionListener mEnableListener;

    public void setEnableActionListener(EnableActionListener enableActionListener) {
        mEnableListener = enableActionListener;
    }

    /**
     * mFieldJTextField接口
     */
    public interface FieldFocusListener {
        void setFieldName(JTextField fieldJTextField);
    }

    private FieldFocusListener mFieldFocusListener;

    public void setFieldFocusListener(FieldFocusListener fieldFocusListener) {
        mFieldFocusListener = fieldFocusListener;
    }

    /**
     * mClickCheckBox接口
     */
    public interface ClickActionListener {
        void setClick(JCheckBox clickCheckBox);
    }

    private ClickActionListener mClickListener;

    public void setClickActionListener(ClickActionListener clickListener) {
        mClickListener = clickListener;
    }

    /**
     * 构造方法
     *
     * @param layout         布局
     * @param emptyBorder    border
     * @param jCheckBox      是否生成+name
     * @param jLabelId       id
     * @param jCheckBoxClick onClick
     * @param jTextField     字段名
     */
    public IdBean(LayoutManager layout, EmptyBorder emptyBorder,
                  JCheckBox jCheckBox, JLabel jLabelId, JCheckBox jCheckBoxClick, JTextField jTextField,
                  Element element) {
        super(layout);
        initLayout(layout, emptyBorder);
        mEnableCheckBox = jCheckBox;
        mIdJLabel = jLabelId;
        mClickCheckBox = jCheckBoxClick;
        mFieldJTextField = jTextField;
        initComponent(element);
        addComponent();
    }

    /**
     * addComponent
     */
    private void addComponent() {
        this.add(mEnableCheckBox);
        this.add(mIdJLabel);
        this.add(mClickCheckBox);
        this.add(mFieldJTextField);
    }

    /**
     * 设置Component
     */
    private void initComponent(Element element) {
        mEnableCheckBox.setSelected(element.isCreateFiled());
        mClickCheckBox.setEnabled(true);
        if (element.isCreateClickMethod()) {
            mClickCheckBox.setSelected(element.isCreateClickMethod());
        }

        mIdJLabel.setEnabled(element.isCreateFiled());
        mFieldJTextField.setEnabled(element.isCreateFiled());

        // 设置左对齐
        mEnableCheckBox.setHorizontalAlignment(JLabel.LEFT);
        mIdJLabel.setHorizontalAlignment(JLabel.LEFT);
        mFieldJTextField.setHorizontalAlignment(JTextField.LEFT);
        // 监听
        mEnableCheckBox.addActionListener(e -> {
            if (mEnableListener != null) {
                mEnableListener.setEnable(mEnableCheckBox,element);
                mIdJLabel.setEnabled(mEnableCheckBox.isSelected());
                mFieldJTextField.setEnabled(mEnableCheckBox.isSelected());
            }
        });
        // 监听
        mClickCheckBox.addActionListener(e -> {
            if (mClickListener != null) {
                mClickListener.setClick(mClickCheckBox);
            }
        });
        // 监听
        mFieldJTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (mFieldFocusListener != null) {
                    mFieldFocusListener.setFieldName(mFieldJTextField);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (mFieldFocusListener != null) {
                    mFieldFocusListener.setFieldName(mFieldJTextField);
                }
            }
        });
    }

    /**
     * 设置布局相关
     *
     * @param layout
     * @param emptyBorder
     */
    private void initLayout(LayoutManager layout, EmptyBorder emptyBorder) {
        // 设置布局内容
        this.setLayout(layout);
        // 设置border
        this.setBorder(emptyBorder);
    }
}
