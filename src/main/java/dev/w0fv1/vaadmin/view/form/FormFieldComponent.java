package dev.w0fv1.vaadmin.view.form;

import dev.w0fv1.vaadmin.view.form.component.BaseFormFieldComponent;
import dev.w0fv1.vaadmin.view.form.component.CustomFormFieldComponentBuilder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface FormFieldComponent {
    Class<? extends CustomFormFieldComponentBuilder> value();
}
