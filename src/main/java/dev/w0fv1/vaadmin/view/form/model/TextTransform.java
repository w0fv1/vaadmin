package dev.w0fv1.vaadmin.view.form.model;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TextTransform {
    /**
     * 一个字符串处理逻辑的类，实现你自定义的接口
     */
    Class<? extends StringConverter> processorClass();
}
