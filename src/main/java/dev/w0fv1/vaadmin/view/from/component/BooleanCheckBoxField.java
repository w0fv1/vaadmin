package dev.w0fv1.vaadmin.view.from.component;

import com.vaadin.flow.component.checkbox.Checkbox;
import dev.w0fv1.vaadmin.view.model.form.BaseFormModel;

import java.lang.reflect.Field;

public class BooleanCheckBoxField extends BaseFormField<Boolean> {
    private final Checkbox checkBox;

    public BooleanCheckBoxField(Field field, BaseFormModel formModel) {
        super(field, formModel);

        checkBox = new Checkbox();

        this.checkBox.setLabel(getFormField().title());

        this.checkBox.setId(field.getName()); // 设置唯一的 fieldId

        Boolean modelData = getModelData();
        if (modelData != null) {
            this.checkBox.setValue(modelData);
        }

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
    public void clear() {
        this.checkBox.clear();
    }
}
