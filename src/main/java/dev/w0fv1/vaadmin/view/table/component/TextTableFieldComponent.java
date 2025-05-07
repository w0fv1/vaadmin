package dev.w0fv1.vaadmin.view.table.component;

import com.vaadin.flow.component.textfield.TextArea;
import dev.w0fv1.vaadmin.view.table.model.TableField;

import java.lang.reflect.Field;

public class TextTableFieldComponent extends BaseFieldComponent<String> {

    public TextTableFieldComponent(Field field, String value) {
        super(field, value);

        String label = "";

        if (field.isAnnotationPresent(TableField.class)) {
            TableField tableField = field.getAnnotation(TableField.class);
            if (tableField != null && tableField.displayName() != null && !tableField.displayName().isEmpty()) {
                label = tableField.displayName();
            } else {
                label = field.getName();
            }
        } else {
            label = field.getName();
        }

        TextArea tf = new TextArea(label);
        tf.setValue(value != null ? value : "");
        tf.setReadOnly(true);
        tf.setWidthFull();

        add(tf);
    }


}
