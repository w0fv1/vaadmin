package dev.w0fv1.vaadmin.view.model.form;

import dev.w0fv1.mapper.Mapper;
import dev.w0fv1.vaadmin.entity.BaseManageEntity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface FormEntitySelectField {
    EntityField entityField();
}
