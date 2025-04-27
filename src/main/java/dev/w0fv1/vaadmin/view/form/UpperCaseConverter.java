package dev.w0fv1.vaadmin.view.form;

import dev.w0fv1.vaadmin.view.form.model.StringConverter;

public class UpperCaseConverter implements StringConverter {
    @Override
    public String convert(String original) {
        if (original == null) return null;
        return original.toUpperCase();
    }
}
