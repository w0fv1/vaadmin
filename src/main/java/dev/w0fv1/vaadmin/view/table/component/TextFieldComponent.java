package dev.w0fv1.vaadmin.view.table.component;

import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;

import java.lang.reflect.Field;

public class TextFieldComponent extends  BaseFieldComponent<String>{

    public TextFieldComponent(Field field, String value) {
        super(field, value);

        String label = field.getName();
        TextArea tf = new TextArea(label);
        tf.setValue(value != null ? value : "");
        tf.setReadOnly(true);
        tf.setWidthFull();

        add(tf);
    }


}
