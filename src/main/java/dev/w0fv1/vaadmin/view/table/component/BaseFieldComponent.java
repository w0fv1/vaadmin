package dev.w0fv1.vaadmin.view.table.component;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import dev.w0fv1.vaadmin.view.model.form.BaseFormModel;

import java.lang.reflect.Field;

public class BaseFieldComponent<Type> extends VerticalLayout {

    private final Field field;
    private Type value;

    public BaseFieldComponent(Field field, Type value) {
        this.field = field;
        this.value = value;
        setWidthFull();
        setPadding(false);
    }

}
