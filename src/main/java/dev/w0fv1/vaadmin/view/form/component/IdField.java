package dev.w0fv1.vaadmin.view.form.component;

import com.vaadin.flow.component.textfield.NumberField;
import dev.w0fv1.vaadmin.view.model.form.BaseFormModel;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;

@Slf4j
public class IdField extends BaseFormFieldComponent<Double> {

    private  NumberField numberField;

    public IdField(Field field, BaseFormModel formModel) {
        super(field, formModel);

    }

    @Override
    public void initView() {

        this.numberField = new NumberField();

        this.numberField.setId(getField().getName()); // 设置唯一的 fieldId

        this.numberField.setPlaceholder("请输入 " + getFormField().title()); // 占位符

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
    public void clearUI() {
        this.numberField.clear();
    }
}
