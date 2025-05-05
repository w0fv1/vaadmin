package dev.w0fv1.vaadmin.view.form.component;

import com.vaadin.flow.component.textfield.TextField;
import dev.w0fv1.vaadmin.view.form.model.BaseFormModel;

import java.lang.reflect.Field;

/**
 * TextInputField
 * 单行文本输入框，绑定 String 数据。
 */
public class TextInputField extends BaseFormFieldComponent<String> {

    private TextField textField; // UI控件
    private String data = ""; // 内部持有数据

    public TextInputField(Field field, BaseFormModel formModel) {
        super(field, formModel);
        super.initialize();

    }

    @Override
    void initStaticView() {
        this.textField = new TextField();
        this.textField.setId(getField().getName());
        this.textField.setPlaceholder("请输入 " + getFormField().title());
        this.textField.setEnabled(getFormField().enabled());
        this.textField.setWidthFull();
        this.textField.setReadOnly(!getFormField().enabled()); // 只有enabled=false时才设置readonly=true

        this.textField.addValueChangeListener(e -> {
            // 只更新内部数据，不直接控制UI
            setData(e.getValue());
        });

        add(this.textField);
    }



    @Override
    public void pushViewData() {
        if (data != null && !data.equals(textField.getValue())) {
            this.textField.setValue(data);
        }
    }

    @Override
    public String getData() {
        return data;
    }

    @Override
    public void setData(String data) {
        this.data = data == null ? "" : data;
    }

    @Override
    public void clearData() {
        this.data = "";
    }

    @Override
    public void clearUI() {
        if (this.textField != null) {
            this.textField.clear();
        }
    }
}
