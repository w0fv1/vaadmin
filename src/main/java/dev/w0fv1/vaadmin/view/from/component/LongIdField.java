package dev.w0fv1.vaadmin.view.from.component;

import com.vaadin.flow.component.textfield.NumberField;
import dev.w0fv1.vaadmin.view.model.form.BaseFormModel;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;

@Slf4j
public class LongIdField extends BaseFormField<Long> {

    private final NumberField numberField;

    public LongIdField(Field field, BaseFormModel formModel) {
        super(field, formModel);

        this.numberField = new NumberField();

        this.numberField.setId(field.getName()); // 设置唯一的 fieldId

        this.numberField.setPlaceholder("请输入 " + getFormField().title()); // 占位符

        Long modelData = getModelData();

        if (modelData != null) {
            this.numberField.setValue(modelData.doubleValue());
        }

        this.numberField.setEnabled(getFormField().enabled());
        this.numberField.setEnabled(false);

        this.add(numberField);
    }

    @Override
    public Long getData() {
        return this.numberField.getValue().longValue();
    }

    @Override
    public void setData(Long data) {
        this.numberField.setValue(data.doubleValue());
    }

    @Override
    public void clear() {
        this.numberField.clear();
    }
}
