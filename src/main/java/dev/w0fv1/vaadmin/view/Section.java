package dev.w0fv1.vaadmin.view;


import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import javax.swing.*;

public class Section extends VerticalLayout {
    private VerticalLayout verticalLayout = new VerticalLayout();

    public Section(String title, String description) {
        super();
        add(new H1(title));
        add(new Span(description));
        add(verticalLayout);
    }

    public void addModule(Component component){
        verticalLayout.add(component);
    }
}
