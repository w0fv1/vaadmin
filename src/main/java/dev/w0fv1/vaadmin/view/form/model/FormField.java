package dev.w0fv1.vaadmin.view.form.model;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface FormField {
    boolean id() default false;
    String defaultValue() default "";

    String title() default "";

    double order() default 100;

    String description() default "";

    boolean enabled() default true;

    boolean display() default true;

    boolean onlyUpdate() default false;


    boolean nullable() default true;

    Class<?> subType() default Object.class;

    boolean longText() default false;

    public static class DefaultFormField implements FormField {

        @Override
        public boolean id() {
            return false;
        }

        @Override
        public String defaultValue() {
            return "";
        }

        @Override
        public String title() {
            return "";
        }

        @Override
        public double order() {
            return 100;
        }
        @Override
        public String description() {
            return "";
        }
        @Override
        public boolean enabled() {
            return true;
        }

        @Override
        public boolean display() {
            return true;
        }

        @Override
        public boolean onlyUpdate() {
            return false;
        }

        @Override
        public boolean nullable() {
            return false;
        }

        @Override
        public Class<?> subType() {
            return Object.class;
        }

        @Override
        public boolean longText() {
            return false;
        }

        @Override
        public Class<? extends java.lang.annotation.Annotation> annotationType() {
            return FormField.class;
        }
    }

}
