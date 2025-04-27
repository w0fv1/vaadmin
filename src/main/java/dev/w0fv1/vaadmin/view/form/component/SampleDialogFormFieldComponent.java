package dev.w0fv1.vaadmin.view.form.component;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import dev.w0fv1.vaadmin.view.form.model.BaseFormModel;

import java.lang.reflect.Field;

public class SampleDialogFormFieldComponent extends BaseDialogFormFieldComponent<String> {

    private TextField textField;

    public SampleDialogFormFieldComponent(Field field, BaseFormModel formModel) {
        super(field, formModel);
    }

    /**
     * 在主界面上添加一个只读文本框用于显示数据，然后调用父类 initView() 初始化对话框及按钮
     */
    @Override
    public void initView() {
        // 添加主界面显示组件（只读文本框）
        textField = new TextField();
        textField.setId(getField().getName());
        textField.setPlaceholder("点击按钮选择内容");
        textField.setReadOnly(true);
        add(textField);
        // 初始化对话框及打开按钮
        super.initView();
    }

    /**
     * 定制对话框内部内容：包含一个输入框和确认按钮
     */
    @Override
    protected VerticalLayout createDialogContent() {
        VerticalLayout dialogLayout = new VerticalLayout();
        // 对话框内的输入框
        TextField dialogTextField = new TextField("请输入内容");
        dialogLayout.add(dialogTextField);
        // 确认按钮：更新主界面文本框、保存数据并关闭对话框
        Button confirmButton = new Button("确认", event -> {
            String value = dialogTextField.getValue();
            textField.setValue(value);
            setData(value);
            dialog.close();
        });
        dialogLayout.add(confirmButton);
        return dialogLayout;
    }

    @Override
    public String getData() {
        return textField.getValue();
    }

    @Override
    public void setData(String data) {
        textField.setValue(data);
    }

    @Override
    public void clearUI() {
        textField.clear();
    }

    public static class SampleDialogFormFieldComponentBuilder implements CustomFormFieldComponentBuilder {

        @Override
        public BaseFormFieldComponent<?> build(Field field, BaseFormModel formModel) {
            return new SampleDialogFormFieldComponent(field, formModel);
        }
    }
}
