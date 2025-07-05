package dev.w0fv1.vaadmin.view.table.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TableField {
    public boolean id() default false;

    public int order() default 100;

    public String key() default "";

    public String displayName() default "";

    public String defaultValue() default "无";

    public boolean editable() default false;

    public boolean likeSearch() default false;

    public boolean sortable() default false;

    public String likeSearchName() default "";
    boolean frozen() default false;

    /**
     * 指定使用的组件类（暂时保留接口，当前逻辑只支持String->TextFieldComponent）
     */
    Class<?> customComponent() default Void.class;
    /*---------------------------------- 新增属性 ----------------------------------*/
    /**
     * 指定字段在数据库中的 SQL 类型（影响模糊搜索时的 cast 方式）。<br/>
     * - {@link SqlType#AUTO}：自动根据 Java 类型推断；<br/>
     * - {@link SqlType#JSONB}：显式声明 json/jsonb 字段，生成 <code>::text</code> cast；<br/>
     * - 其余枚举值可按需扩展。
     */
    SqlType sqlType() default SqlType.AUTO;

    /**
     * SQL 类型枚举。
     */
    enum SqlType {
        /** 自动推断（String→TEXT；Number/Date→NUMERIC；List→JSONB） */
        AUTO,
        TEXT,
        NUMERIC,
        DATE,
        JSONB
    }

}
