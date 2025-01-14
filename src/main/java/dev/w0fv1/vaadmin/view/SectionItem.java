package dev.w0fv1.vaadmin.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class SectionItem extends VerticalLayout {
    private final VerticalLayout verticalLayout = new VerticalLayout();

    public SectionItem(String title, String description) {
        super();
        add(new H4(title));
        if (description != null && !description.isEmpty()) {
            add(new Span(description));
        }
        add(verticalLayout);
        verticalLayout.setPadding(false);
        setPadding(false);
    }
    public SectionItem(String title) {
        this(title, "");
    }
    public void addSubModule(Component component){
        verticalLayout.add(component);
    }
}
