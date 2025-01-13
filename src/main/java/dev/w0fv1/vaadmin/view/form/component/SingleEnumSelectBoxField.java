package dev.w0fv1.vaadmin.view.form.component;

import dev.w0fv1.vaadmin.view.model.form.BaseFormModel;

import java.lang.reflect.Field;

import com.vaadin.flow.component.combobox.ComboBox;

public class SingleEnumSelectBoxField extends BaseFormFieldComponent<Enum<?>> {
    private ComboBox<Enum<?>> comboBox;

    public SingleEnumSelectBoxField(Field field, BaseFormModel formModel) {
        super(field, formModel);


    }

    @Override
    public void initView() {
        this.comboBox = new ComboBox<>();
        this.comboBox.setItems((Enum<?>[]) getField().getType().getEnumConstants());
        this.comboBox.setPlaceholder("请选择 " + getFormField().title());
        this.comboBox.setId(getField().getName());

        // 设置是否可用
        this.comboBox.setEnabled(getFormField().enabled());

        // 添加到组件中
        this.add(this.comboBox);
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
    public void clearUI() {
        this.comboBox.clear();
    }
}
