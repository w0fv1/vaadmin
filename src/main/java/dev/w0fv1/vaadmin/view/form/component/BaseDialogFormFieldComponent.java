package dev.w0fv1.vaadmin.view.form.component;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import dev.w0fv1.vaadmin.view.form.model.BaseFormModel;
import lombok.Getter;

import java.lang.reflect.Field;

/**
 * 基于对话框的表单字段组件。
 * 继承自 BaseFormFieldComponent，并扩展 Dialog 弹出框功能。
 */
@Getter
public abstract class BaseDialogFormFieldComponent<Type> extends BaseFormFieldComponent<Type> {

    protected Dialog dialog;
    protected Button openDialogButton;

    public BaseDialogFormFieldComponent(Field field, BaseFormModel formModel) {
        super(field, formModel);
    }

    /**
     * 覆盖 initView() 方法，在主界面添加对话框按钮，并初始化 Dialog
     */
    @Override
    public void initView() {
        // 子类可在自己的 initView() 中添加主界面显示组件后，再调用 super.initView() 来初始化对话框部分
        initDialog();
        addOpenDialogButton("打开对话框");
    }

    /**
     * 初始化 Dialog 弹出框，并添加自定义内容
     */
    protected void initDialog() {
        dialog = new Dialog();
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);
        dialog.add(createDialogContent());
    }

    /**
     * 用于构建对话框内部内容的抽象方法，子类必须实现此方法来自定义对话框内容
     * @return 对话框内部的 VerticalLayout 布局
     */
    protected abstract VerticalLayout createDialogContent();

    /**
     * 打开对话框
     */
    protected void openDialog() {
        dialog.open();
    }

    /**
     * 添加用于打开对话框的按钮
     * @param buttonText 按钮显示文本
     */
    protected void addOpenDialogButton(String buttonText) {
        openDialogButton = new Button(buttonText, event -> openDialog());
        add(openDialogButton);
    }
}
