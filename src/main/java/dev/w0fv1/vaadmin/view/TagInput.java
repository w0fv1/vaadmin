package dev.w0fv1.vaadmin.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.List;
import java.util.function.Consumer;

/**
 * TagInput 组件包含 TagGroup、TextField 和添加按钮，用于输入和管理标签。
 */
public class TagInput extends VerticalLayout {
    private final TagGroup tagGroup;
    private final TextField inputField;
    private final Button addButton;

    /**
     * 构造方法，初始化 TagInput 组件。
     */
    public TagInput() {
        setSpacing(false);
        setPadding(false);

        // 初始化 TagGroup
        tagGroup = new TagGroup();

        // 初始化输入区域
        inputField = new TextField();
        inputField.setPlaceholder("输入标签");
        inputField.setWidthFull();

        addButton = new Button("添加");
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClickListener(event -> addTag());

        // 输入布局
        HorizontalLayout inputLayout = new HorizontalLayout(inputField, addButton);
        inputLayout.setWidthFull();
        inputLayout.setSpacing(true);
        inputLayout.setPadding(false);

        // 将 TagGroup 和输入布局添加到 TagInput 中
        add(tagGroup, inputLayout);
    }

    /**
     * 添加新标签的方法，从输入框获取值并添加到 TagGroup。
     */
    public void addTag() {
        String value = inputField.getValue().trim();
        if (!value.isEmpty()) {
            addTag(value);
            inputField.clear();
        }
    }

    public void addTag(String tag) {
        tagGroup.addTag(tag);
    }

    /**
     * 设置单个标签（包装 TagGroup 的 setTag 方法）
     *
     * @param tags 新标签
     */
    public void setTags(List<String> tags) {
        tagGroup.setTags(tags);
    }


    /**
     * 获取当前的标签列表。
     *
     * @return 标签列表
     */
    public List<String> getTags() {
        return tagGroup.getTags();
    }

    /**
     * 设置标签变化的监听器。
     *
     * @param onChange 标签变化时的消费者
     */
    public void setOnChangeListener(Consumer<List<String>> onChange) {
        tagGroup.setOnChangeListener(onChange);
    }

    /**
     * 启用或禁用 TagInput 组件。
     *
     * @param enabled true 为启用，false 为禁用
     */
    @Override
    public void setEnabled(boolean enabled) {
        inputField.setEnabled(enabled);
        addButton.setEnabled(enabled);
        tagGroup.setEnabled(enabled);
    }

    /**
     * 清空所有标签和输入框内容。
     */
    public void clear() {
        tagGroup.clearTags();
        inputField.clear();
    }
}
