package dev.w0fv1.vaadmin.view.form.component;


import com.vaadin.flow.component.textfield.TextField;
import dev.w0fv1.vaadmin.view.form.model.BaseFormModel;

import java.lang.reflect.Field;

public class SampleFormFieldComponent extends BaseFormFieldComponent<String> {

    private TextField textField;

    public SampleFormFieldComponent(Field field, BaseFormModel formModel) {
        super(field, formModel);


    }

    @Override
    public void initView() {

        this.textField = new TextField();
        this.textField.setId(getField().getName()); // 设置唯一的 fieldId

        this.textField.setPlaceholder("请输入 " + getFormField().title() + "这是一个定制化Form组件示例"); // 占位符

        this.textField.setEnabled(getFormField().enabled());

        this.add(textField);
    }

    @Override
    public String getData() {
        return this.textField.getValue();
    }

    @Override
    public void setData(String data) {
        this.textField.setValue(data);
    }

    @Override
    public void clearUI() {
        this.textField.clear();
    }
}
