package dev.w0fv1.vaadmin.view;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class Section extends VerticalLayout {
    private final VerticalLayout verticalLayout = new VerticalLayout();

    public Section(String title, String description) {
        super();
        add(new H3(title));
        if (description != null && !description.isEmpty()) {
            add(new Span(description));
        }
        add(verticalLayout);
        verticalLayout.setPadding(false);
    }



    public void addModule(Component component) {
        verticalLayout.add(component);
    }
}
