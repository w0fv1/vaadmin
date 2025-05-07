package dev.w0fv1.vaadmin.view;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

/**
 * 错误消息提示组件（正确实现版）
 */
public class ErrorMessage extends Span {

    private final Icon icon;
    private final Span messageSpan;

    public ErrorMessage(String message) {
        icon = VaadinIcon.EXCLAMATION_CIRCLE_O.create();
        icon.getStyle().set("padding", "var(--lumo-space-xs)");

        messageSpan = new Span(message);

        this.add(icon, messageSpan);
        this.getElement().getThemeList().add("badge error");
        this.setVisible(true);  // 有信息则默认显示
    }

    public ErrorMessage() {
        icon = VaadinIcon.EXCLAMATION_CIRCLE_O.create();
        icon.getStyle().set("padding", "var(--lumo-space-xs)");

        messageSpan = new Span("");

        this.add(icon, messageSpan);
        this.getElement().getThemeList().add("badge error");
        this.setVisible(false);  // 无信息则默认不显示
    }

    @Override
    public void setText(String text) {
        messageSpan.setText(text);
        this.setVisible(text != null && !text.isEmpty());  // 自动根据内容控制可见性
    }
}
