package dev.w0fv1.vaadmin.view.form.component;

import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import dev.w0fv1.vaadmin.view.ErrorMessage;
import dev.w0fv1.vaadmin.view.model.form.FormField;
import dev.w0fv1.vaadmin.view.model.form.BaseFormModel;
import lombok.Getter;

import java.lang.reflect.Field;

import static dev.w0fv1.vaadmin.component.FileValidator.validFile;
import static dev.w0fv1.vaadmin.util.TypeUtil.defaultIfNull;

@Getter
public abstract class BaseFormFieldComponent<Type> extends VerticalLayout {
    private final Field field;
    private final BaseFormModel formModel;
    private final FormField formField;

    private ErrorMessage errorMessage;

    public BaseFormFieldComponent(Field field, BaseFormModel formModel) {
        this.field = field;
        this.formModel = formModel;
        this.formField = field.getAnnotation(FormField.class);
        this.setPadding(false);
        buildTitle();
    }

    public void buildTitle() {
        String title = formField.title().isEmpty() ? field.getName() : formField.title();
        if (!formField.enabled()) {
            title += "(不可编辑)";
        }
        add(new H3(title));
        if (formField.description() != null && !formField.description().isEmpty()) {
            add(new Span(formField.description()));
        }

    }


    public abstract Type getData();

    public void save() {
        setModelData();
    }

    public abstract void setData(Type data);

    @SuppressWarnings("unchecked")
    public Type getModelData() {
        getField().setAccessible(true);
        Type data = null;
        try {
            data = (Type) getField().get(this.getFormModel());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return (Type) defaultIfNull(data, getField().getType());
    }

    public Boolean valid() {
        String valid = validFile(field, formModel);
        if (valid != null && !valid.isEmpty()) {
            if (errorMessage == null) {
                errorMessage = new ErrorMessage(valid);
                add(errorMessage);
            } else {
                errorMessage.setText(valid);
            }
            return false;
        } else if (errorMessage != null) {
            remove(errorMessage);
        }
        return true;
    }

    public void setModelData() {
        getField().setAccessible(true);
        try {
            getField().set(this.getFormModel(), getData());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public abstract void clear();
}
