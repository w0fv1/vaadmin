package dev.w0fv1.vaadmin.view.from.component;

import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import dev.w0fv1.vaadmin.view.model.form.BaseFormModel;

import java.lang.reflect.Field;


public class LongTextInputField extends BaseFormField<String> {


    private final TextArea textField;

    public LongTextInputField(Field field, BaseFormModel formModel) {
        super(field, formModel);


        this.textField = new TextArea();
        this.textField.setId(field.getName()); // 设置唯一的 fieldId

        this.textField.setPlaceholder("请输入 " + getFormField().title()); // 占位符
        String modelData = getModelData();
        if (modelData != null) {
            this.textField.setValue(modelData);
        }

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
    public void clear() {
        this.textField.clear();
    }

}
