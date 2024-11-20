package dev.w0fv1.vaadmin.view;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

public class ErrorMessage extends Span {
    public ErrorMessage(String message) {
        Icon icon = VaadinIcon.EXCLAMATION_CIRCLE_O.create();
        icon.getStyle().set("padding", "var(--lumo-space-xs)");
        this.add(icon);
        this.add(new Span(message));
        this.getElement().getThemeList().add("badge error");
    }

    @Override
    public void setText(String text) {
        super.setText("");
        Icon icon = VaadinIcon.EXCLAMATION_CIRCLE_O.create();
        icon.getStyle().set("padding", "var(--lumo-space-xs)");
        this.add(icon);
        this.add(icon);
        this.add(new Span(text));
    }
}
