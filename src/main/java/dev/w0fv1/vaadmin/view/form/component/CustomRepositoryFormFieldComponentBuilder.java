package dev.w0fv1.vaadmin.view.form.component;

import dev.w0fv1.vaadmin.GenericRepository;
import dev.w0fv1.vaadmin.view.form.model.BaseFormModel;

import java.lang.reflect.Field;

public interface CustomRepositoryFormFieldComponentBuilder {

    BaseFormFieldComponent<?> build(Field field, BaseFormModel formModel, GenericRepository genericRepository);
}
