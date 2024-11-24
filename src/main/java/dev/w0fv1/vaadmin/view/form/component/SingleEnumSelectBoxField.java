package dev.w0fv1.vaadmin.view.form.component;

import dev.w0fv1.vaadmin.view.model.form.BaseFormModel;

import java.lang.reflect.Field;

import com.vaadin.flow.component.combobox.ComboBox;

public class SingleEnumSelectBoxField extends BaseFormFieldComponent<Enum<?>> {
    private final ComboBox<Enum<?>> comboBox;

    public SingleEnumSelectBoxField(Field field, BaseFormModel formModel) {
        super(field, formModel);

        comboBox = new ComboBox<>();
        comboBox.setItems((Enum<?>[]) field.getType().getEnumConstants());
        comboBox.setPlaceholder("请选择 " + getFormField().title());
        comboBox.setId(field.getName());

        // 初始化值
        Enum<?> initialValue = getModelData();
        if (initialValue != null) {
            comboBox.setValue(initialValue);
        }

        // 设置是否可用
        comboBox.setEnabled(getFormField().enabled());

        // 添加到组件中
        this.add(comboBox);
    }


    @Override
    public Enum<?> getData() {
        return this.comboBox.getValue();
    }

    @Override
    public void setData(Enum<?> data) {
        this.comboBox.setValue(data);
    }

    @Override
    public void clear() {
        this.comboBox.clear();
    }
}
