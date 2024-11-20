package dev.w0fv1.vaadmin.component;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class FileValidator {
    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private static final Validator validator = factory.getValidator();

    public static String validFile(Field field, Object object) {
        Class<?> beanClass = field.getDeclaringClass();
        // 获取字段名称
        String propertyName = field.getName();

        Object value = null;

        try {
            value =  field.get(object);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
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
