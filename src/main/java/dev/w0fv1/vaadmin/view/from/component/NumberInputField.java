package dev.w0fv1.vaadmin.view.from.component;

import com.vaadin.flow.component.textfield.NumberField;
import dev.w0fv1.vaadmin.view.model.form.BaseFormModel;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;

@Slf4j
public class NumberInputField extends BaseFormField<Double> {

    private final NumberField numberField;

    public NumberInputField(Field field, BaseFormModel formModel) {
        super(field, formModel);

        this.numberField = new NumberField();

        this.numberField.setId(field.getName()); // 设置唯一的 fieldId

        this.numberField.setPlaceholder("请输入 " + getFormField().title()); // 占位符

        Double modelData  = getModelData();

        if (modelData != null) {
            this.numberField.setValue(modelData);
        }

        this.numberField.setEnabled(getFormField().enabled());

        this.add(numberField);
    }

    @Override
    public Double getData() {
        return this.numberField.getValue();
    }

    @Override
    public void setData(Double data) {
        this.numberField.setValue(data);
    }

    @Override
    public void clear() {
        this.numberField.clear();
    }
}
