package dev.w0fv1.vaadmin.view.form;

import dev.w0fv1.vaadmin.view.model.form.StringTransformer;

public class UpperCaseTransformer implements StringTransformer {
    @Override
    public String transform(String original) {
        if (original == null) return null;
        return original.toUpperCase();
    }
}
