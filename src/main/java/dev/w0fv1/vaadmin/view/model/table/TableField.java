package dev.w0fv1.vaadmin.view.model.table;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TableField {
    public int order() default 100;

    public String displayName() default "";

    public String defaultValue() default "无";

    public boolean editable() default false;

    public boolean likeSearch() default false;

    public String likeSearchName() default "";

}
