package dev.w0fv1.vaadmin.view.form.component;

import dev.w0fv1.vaadmin.view.model.form.BaseFormModel;
import lombok.Getter;

import java.lang.reflect.Field;

@Getter
public class SampleFormFieldComponentBuilder implements CustomFormFieldComponentBuilder {

    @Override
    public SampleFormFieldComponent build(Field field, BaseFormModel formModel) {
        return new SampleFormFieldComponent(field, formModel);
    }
}
