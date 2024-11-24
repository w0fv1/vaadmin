package dev.w0fv1.vaadmin.view.form.component;

import dev.w0fv1.vaadmin.view.model.form.BaseFormModel;

import java.lang.reflect.Field;

public interface CustomFormFieldComponentBuilder {

    BaseFormFieldComponent<?> build(Field field, BaseFormModel formModel);
}
