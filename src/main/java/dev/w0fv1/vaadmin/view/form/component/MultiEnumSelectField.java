package dev.w0fv1.vaadmin.view.form.component;

import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import dev.w0fv1.vaadmin.view.model.form.BaseFormModel;
import dev.w0fv1.vaadmin.view.model.form.FormField;

import java.lang.reflect.Field;
import java.util.List;

public class MultiEnumSelectField extends BaseFormFieldComponent<List<Enum<?>>> {
    private MultiSelectComboBox<Enum<?>> multiSelectComboBox;

    public MultiEnumSelectField(Field field, BaseFormModel formModel) {
        super(field, formModel);


    }

    @Override
    public void initView() {
        FormField formField = getField().getAnnotation(FormField.class);
        Class<?> subType = formField.subType();

        multiSelectComboBox = new MultiSelectComboBox<>();
        multiSelectComboBox.setItems((Enum<?>[]) subType.getEnumConstants());
        multiSelectComboBox.setPlaceholder("请选择 " + getFormField().title());
        multiSelectComboBox.setId(getField().getName());

        // 初始化值
        List<Enum<?>> initialValues = getModelData();
        if (initialValues != null) {
            multiSelectComboBox.setValue(initialValues);
        }

        // 设置是否可用
        multiSelectComboBox.setEnabled(getFormField().enabled());

        // 添加到组件中
        this.add(multiSelectComboBox);
    }


    @Override
    public List<Enum<?>> getData() {
        return this.multiSelectComboBox.getValue().stream().toList();
    }

    @Override
    public void setData(List<Enum<?>> data) {
        this.multiSelectComboBox.setValue(data);
    }

    @Override
    public void clearUI() {
        this.multiSelectComboBox.clear();
    }
}
