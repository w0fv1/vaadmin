package dev.w0fv1.vaadmin.view.form.component;

import com.vaadin.flow.component.textfield.NumberField;
import dev.w0fv1.vaadmin.view.model.form.BaseFormModel;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;

@Slf4j
public class NumberInputField<T extends Number> extends BaseFormFieldComponent<T> {

    private NumberField numberField;
    private Class<T> type;

    /**
     * 构造函数，增加类型参数
     *
     * @param field     字段反射对象
     * @param formModel 表单模型
     * @param type      数字类型的Class对象
     */
    public NumberInputField(Field field, BaseFormModel formModel, Class<T> type) {
        super(field, formModel);
        this.type = type;
        if (type.equals(Integer.class) || type.equals(Long.class)) {
            this.numberField.setStep(1);
        } else {
            this.numberField.setStep(0.1);
        }
    }

    @Override
    public void initView() {
        this.numberField = new NumberField();

        this.numberField.setId(getField().getName()); // 设置唯一的 fieldId

        this.numberField.setPlaceholder("请输入 " + getFormField().title()); // 占位符

        this.numberField.setEnabled(getFormField().enabled());

        // 根据类型设置适当的步长（可选）


        this.add(numberField);
    }

    @Override
    public T getData() {
        Double value = this.numberField.getValue();
        if (value == null) {
            return null;
        }

        try {
            if (type.equals(Double.class)) {
                return type.cast(value);
            } else if (type.equals(Float.class)) {
                return type.cast(value.floatValue());
            } else if (type.equals(Long.class)) {
                return type.cast(value.longValue());
            } else if (type.equals(Integer.class)) {
                return type.cast(value.intValue());
            } else if (type.equals(Byte.class)) {
                return type.cast(value.byteValue());
            } else if (type.equals(Short.class)) {
                return type.cast(value.shortValue());
            } else {
                throw new UnsupportedOperationException("Unsupported number type: " + type.getName());
            }
        } catch (ClassCastException e) {
            log.error("Type casting failed for type: {}", type.getName(), e);
            return null;
        }
    }

    @Override
    public void setData(T data) {
        if (data == null) {
            this.numberField.clear();
            return;
        }
        this.numberField.setValue(data.doubleValue());
    }

    @Override
    public void clear() {
        this.numberField.clear();
    }
}
