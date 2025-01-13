package dev.w0fv1.vaadmin.view.form.component;

import com.vaadin.flow.component.checkbox.Checkbox;
import dev.w0fv1.vaadmin.view.model.form.BaseFormModel;

import java.lang.reflect.Field;

public class BooleanCheckBoxField extends BaseFormFieldComponent<Boolean> {
    private Checkbox checkBox;

    public BooleanCheckBoxField(Field field, BaseFormModel formModel) {
        super(field, formModel);
    }

    @Override
    public void initView() {
        this.checkBox = new Checkbox();

        this.checkBox.setLabel(getFormField().title());

        this.checkBox.setId(getField().getName()); // 设置唯一的 fieldId

        this.checkBox.setEnabled(getFormField().enabled());

        this.add(this.checkBox);
    }


    @Override
    public Boolean getData() {
        return this.checkBox.getValue();
    }

    @Override
    public void setData(Boolean data) {
        this.checkBox.setValue(data);
    }

    @Override
    public void clearUI() {
        this.checkBox.clear();
    }
}
