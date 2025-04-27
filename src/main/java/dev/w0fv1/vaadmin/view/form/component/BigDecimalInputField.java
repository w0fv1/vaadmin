package dev.w0fv1.vaadmin.view.form.component;

import com.vaadin.flow.component.textfield.NumberField;
import dev.w0fv1.vaadmin.view.form.model.BaseFormModel;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.math.BigDecimal;

@Slf4j
public class BigDecimalInputField extends BaseFormFieldComponent<BigDecimal> {

    private NumberField numberField;

    public BigDecimalInputField(Field field, BaseFormModel formModel) {
        super(field, formModel);
    }

    @Override
    public void initView() {
        this.numberField = new NumberField();

        this.numberField.setId(getField().getName()); // 设置唯一的 fieldId
        this.numberField.setPlaceholder("请输入 " + getFormField().title()); // 占位符
        this.numberField.setEnabled(getFormField().enabled());
        this.numberField.setStep(0.01); // 设置默认步长
        
        this.add(numberField);
    }

    @Override
    public BigDecimal getData() {
        Double value = this.numberField.getValue();
        return (value != null) ? BigDecimal.valueOf(value) : null;
    }

    @Override
    public void setData(BigDecimal data) {
        if (data == null) {
            this.numberField.clear();
            return;
        }
        this.numberField.setValue(data.doubleValue());
    }

    @Override
    public void clearUI() {
        this.numberField.clear();
    }
}
