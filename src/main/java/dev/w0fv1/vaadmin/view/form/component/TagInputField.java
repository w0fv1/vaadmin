package dev.w0fv1.vaadmin.view.form.component;

import dev.w0fv1.vaadmin.view.model.form.BaseFormModel;
import dev.w0fv1.vaadmin.view.TagInput;

import java.lang.reflect.Field;
import java.util.List;

/**
 * TagInputField 作为表单组件，封装 TagInput，并与表单模型同步。
 */
public class TagInputField extends BaseFormFieldComponent<List<String>> {
    private final TagInput tagInput;

    /**
     * 构造方法，初始化 TagInputField 组件。
     *
     * @param field     表单模型中的字段
     * @param formModel 表单模型实例
     */
    public TagInputField(Field field, BaseFormModel formModel) {
        super(field, formModel);

        tagInput = new TagInput();

        // 设置初始标签列表
        List<String> initialTags = getModelData();
        if (initialTags != null) {
            tagInput.setTags(initialTags);
        }

        // 设置组件的启用状态
        tagInput.setEnabled(getFormField().enabled());

        // 将 TagInput 添加到 TagInputField 中
        add(tagInput);
    }

    @Override
    public List<String> getData() {
        return tagInput.getTags();
    }

    @Override
    public void setData(List<String> data) {
        tagInput.setTags(data);
    }

    @Override
    public void clear() {
        tagInput.clear();
    }
}
