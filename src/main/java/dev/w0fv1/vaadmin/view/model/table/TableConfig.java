package dev.w0fv1.vaadmin.view.model.table;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface TableConfig {
    public String title() default "";
    public String description() default "";

    boolean likeSearch() default false;


}
