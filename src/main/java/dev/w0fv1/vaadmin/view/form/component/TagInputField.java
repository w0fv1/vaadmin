package dev.w0fv1.vaadmin.view.form.component;

import dev.w0fv1.vaadmin.view.form.model.BaseFormModel;
import dev.w0fv1.vaadmin.view.TagInput;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * TagInputField
 * 多标签输入控件，绑定 List<String> 数据。
 */
public class TagInputField extends BaseFormFieldComponent<List<String>> {

    private TagInput tagInput; // UI控件
    private List<String> data = new ArrayList<>(); // 内部持有数据

    public TagInputField(Field field, BaseFormModel formModel) {
        super(field, formModel);
        super.initialize();

    }

    @Override
    void initStaticView() {
        this.tagInput = new TagInput();
        this.tagInput.setEnabled(getFormField().enabled());
        this.tagInput.setOnChangeListener(tags -> {
            // 只更新内部数据，不操作UI
            setData(new ArrayList<>(tags));
        });
        add(this.tagInput);
    }

    @Override
    public void pushViewData() {
        this.tagInput.clear(); // 幂等要求，每次都清空
        if (data != null) {
            for (String tag : data) {
                this.tagInput.addTag(tag);
            }
        }
    }

    @Override
    public List<String> getData() {
        return data;
    }

    @Override
    public void setData(List<String> data) {
        this.data.clear();
        if (data != null) {
            this.data.addAll(data);
        }
    }

    @Override
    public void clearData() {
        this.data.clear();
    }

    @Override
    public void clearUI() {
        if (this.tagInput != null) {
            this.tagInput.clear();
        }
    }
}
