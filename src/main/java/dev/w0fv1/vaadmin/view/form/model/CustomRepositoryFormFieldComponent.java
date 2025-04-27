package dev.w0fv1.vaadmin.view.form.model;

import dev.w0fv1.vaadmin.view.form.component.CustomRepositoryFormFieldComponentBuilder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomRepositoryFormFieldComponent {
    Class<? extends CustomRepositoryFormFieldComponentBuilder> value();
}
