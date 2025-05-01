package dev.w0fv1.vaadmin.component;

import dev.w0fv1.vaadmin.view.form.model.FormField;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.stream.Collectors;

import static dev.w0fv1.vaadmin.util.TypeUtil.isEmpty;

@Component
public class FieldValidator {
    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private static final Validator validator = factory.getValidator();

    public static String validField(Field field, Object object) {
        Class<?> beanClass = field.getDeclaringClass();
        // 获取字段名称
        String propertyName = field.getName();

        Object value = null;

        try {
            value = field.get(object);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        if (field.isAnnotationPresent(FormField.class)) {
            FormField formField = field.getAnnotation(FormField.class);

            if (formField != null && !formField.nullable() && isEmpty(value)) {
                return "值为空，该字段不允许为空";
            }
        }


        // 使用 Validator 的 validateValue 方法，仅验证指定字段的值
        Set<? extends ConstraintViolation<?>> violations = validator.validateValue(beanClass, propertyName, value);

        if (violations.isEmpty()) {
            return ""; // 验证通过
        }

        // 收集所有的验证错误信息，并用逗号分隔
        return violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
    }
}
