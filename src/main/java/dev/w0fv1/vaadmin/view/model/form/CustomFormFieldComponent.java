package dev.w0fv1.vaadmin.view.model.form;

import dev.w0fv1.vaadmin.view.form.component.CustomFormFieldComponentBuilder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomFormFieldComponent {
    Class<? extends CustomFormFieldComponentBuilder> value();
}
