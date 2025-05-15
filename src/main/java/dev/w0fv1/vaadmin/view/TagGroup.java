package dev.w0fv1.vaadmin.view;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexWrap;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * TagGroup 负责管理和渲染标签列表。
 * 利用 FlexLayout 的特性，自动管理标签的间距和换行。
 */
public class TagGroup extends FlexLayout {
    private final List<String> tags = new ArrayList<>();
    private Consumer<List<String>> onChange;

    /**
     * 构造方法，初始化布局属性。
     */
    public TagGroup() {
        // 设置FlexLayout的换行属性，确保标签在溢出时自动换行
        setFlexWrap(FlexWrap.WRAP);
        // 设置标签在容器中的对齐方式
        setAlignItems(Alignment.CENTER);
        // 设置容器的宽度
        setMaxWidth("400px");
        setWidthFull();
        // 设置标签之间的间距，使用内置的 Lumo 变量
        getStyle().set("gap", "var(--lumo-space-xs)"); // 8px
    }

    /**
     * 设置标签变化的监听器。
     *
     * @param onChange 当标签列表变化时调用的消费者
     */
    public void setOnChangeListener(Consumer<List<String>> onChange) {
        this.onChange = onChange;
    }

    /**
     * 添加一个新标签。
     *
     * @param tag 要添加的标签
     */
    public void addTag(String tag) {
        if (!tags.contains(tag) && !tag.isEmpty()) { // 确保标签唯一
            tags.add(tag);
            Span badge = createBadge(tag);
            add(badge);
            notifyChange();
        }
    }

    public void setTags(List<String> tags) {
        this.tags.clear();
        removeAll();

        if (tags != null && !tags.isEmpty()) {
            for (String tag : tags) {
                if (tag != null && !tag.trim().isEmpty()) {
                    this.tags.add(tag);
                    Span badge = createBadge(tag);
                    add(badge);
                }
            }
        }

        notifyChange();
    }

    /**
     * 删除一个标签。
     *
     * @param tag 要删除的标签
     */
    public void removeTag(String tag) {
        if (tags.remove(tag)) {
            // 移除对应的标签组件
            getChildren()
                    .filter(component -> component instanceof Span)
                    .map(component -> (Span) component)
                    .filter(span -> span.getText().equals(tag))
                    .findFirst()
                    .ifPresent(this::remove);
            notifyChange();
        }
    }

    /**
     * 清空所有标签。
     */
    public void clearTags() {
        tags.clear();
        removeAll();
        notifyChange();
    }

    /**
     * 获取当前的标签列表。
     *
     * @return 标签列表
     */
    public List<String> getTags() {
        return new ArrayList<>(tags);
    }

    /**
     * 创建一个带有删除功能的标签。
     *
     * @param tag 标签文本
     * @return 带有删除功能的 Span 组件
     */
    private Span createBadge(String tag) {
        Span badge = new Span(tag);
        // 应用Vaadin的内置badge主题
        badge.getElement().getThemeList().add("badge");
        // 设置固定宽度，确保标签在容器中有一致的大小
        // 设置鼠标悬停时显示为手型，提示可点击删除
        badge.getStyle().set("cursor", "pointer");
        // 点击标签时删除该标签
        badge.addClickListener(event -> removeTag(tag));
        return badge;
    }

    /**
     * 通知变化监听器标签列表已改变。
     */
    private void notifyChange() {
        if (onChange != null) {
            onChange.accept(getTags());
        }
    }
}
