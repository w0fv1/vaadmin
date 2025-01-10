package dev.w0fv1.vaadmin.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.TextField;
import java.util.function.Consumer;

public class TextInput extends Div {

    private final TextField textField;
    private final Button saveButton;
    private final Button resetButton;
    private String initText;

    public TextInput(String initText, Consumer<String> onSaved) {
        this.initText = initText;

        // Set up the container
        this.getStyle().set("display", "flex")
                .set("align-items", "center")
                .set("gap", "10px");

        // Create the text input field
        textField = new TextField();
        textField.setPlaceholder("Enter text...");
        textField.setValue(initText);

        // Create the save button
        saveButton = new Button("Save");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(event -> {
            String inputText = textField.getValue();
            onSaved.accept(inputText);
            this.initText = inputText;
        });

        // Create the reset button
        resetButton = new Button("Reset");
        resetButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        resetButton.addClickListener(event -> {
            textField.setValue(this.initText);
            onSaved.accept(this.initText);
        });

        // Add components to the layout
        add(textField, saveButton, resetButton);
    }
}
