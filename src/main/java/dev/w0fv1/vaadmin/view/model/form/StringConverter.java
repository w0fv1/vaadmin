package dev.w0fv1.vaadmin.view.model.form;

/**
 * 用于对表单String字段做自定义处理的接口
 */
public interface StringConverter {
    /**
     * @param original 原始字符串
     * @return 处理后的字符串
     */
    String convert(String original);
}
