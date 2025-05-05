package dev.w0fv1.vaadmin.view.form.component;

import dev.w0fv1.vaadmin.view.form.model.BaseFormModel;
import com.vaadin.flow.component.combobox.ComboBox;

import java.lang.reflect.Field;

/**
 * SingleEnumSelectBoxField
 * 用于选择单个枚举值的下拉框，绑定 Enum<?> 数据。
 */
public class SingleEnumSelectBoxField extends BaseFormFieldComponent<Enum<?>> {

    private ComboBox<Enum<?>> comboBox; // UI控件
    private Enum<?> data;                // 内部持有数据

    public SingleEnumSelectBoxField(Field field, BaseFormModel formModel) {
        super(field, formModel);
        super.initialize();

    }

    @Override
    void initStaticView() {
        this.comboBox = new ComboBox<>();
        this.comboBox.setItems((Enum<?>[]) getField().getType().getEnumConstants());
        this.comboBox.setPlaceholder("请选择 " + getFormField().title());
        this.comboBox.setId(getField().getName());
        this.comboBox.setWidthFull();
        this.comboBox.setEnabled(getFormField().enabled());

        this.comboBox.addValueChangeListener(event -> {
            setData(event.getValue());
        });

        add(this.comboBox);
    }



    @Override
    public void pushViewData() {
        if (comboBox != null) {
            if (data == null) {
                comboBox.clear();
            } else if (!data.equals(comboBox.getValue())) {
                comboBox.setValue(data);
            }
        }
    }

    @Override
    public Enum<?> getData() {
        return data;
    }

    @Override
    public void setData(Enum<?> data) {
        this.data = data;
    }

    @Override
    public void clearData() {
        this.data = null;
    }

    @Override
    public void clearUI() {
        if (comboBox != null) {
            comboBox.clear();
        }
    }
}
