package dev.w0fv1.vaadmin.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dnd.DragSource;
import com.vaadin.flow.component.dnd.DropTarget;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.*;

import java.util.ArrayList;
import java.util.List;

public class ListBoxGroup<T> extends Scroller {
    public enum Orientation {
        HORIZONTAL,
        VERTICAL
    }

    public ListBoxGroup() {
        this(Orientation.HORIZONTAL);
    }

    public interface OnOrderChangeListener<T> {
        void onOrderChange(List<T> newOrder);
    }

    private final List<ListBox<T>> listBoxes = new ArrayList<>();
    private Button addButton;
    private FlexComponent listBoxLayout;
    private FlexComponent layout;
    private OnOrderChangeListener<T> orderChangeListener;
    private Orientation orientation;

    public ListBoxGroup(Orientation orientation) {
        super();
        this.orientation = orientation;
        this.build();
    }

    public void build() {
        if (orientation.equals(Orientation.HORIZONTAL)) {
            listBoxLayout = new HorizontalLayout();
            layout = new HorizontalLayout();
        } else {
            listBoxLayout = new VerticalLayout();
            layout = new VerticalLayout();
        }
        ((ThemableLayout) listBoxLayout).setPadding(false);
        ((ThemableLayout) layout).setPadding(false);

        // Add the "+" button
        addButton = new Button("+");
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        layout.add(addButton);


        layout.add((Component) listBoxLayout);

        if (orientation.equals(Orientation.HORIZONTAL)) {
            this.setScrollDirection(Scroller.ScrollDirection.HORIZONTAL);
        } else {
            this.setScrollDirection(Scroller.ScrollDirection.VERTICAL);
        }
        this.setContent((Component) layout);
    }

    private Boolean setAddButton = true;

    public void addListBox(ListBox<T> listBox) {
        if (setAddButton) {
            if (orientation.equals(Orientation.VERTICAL)) {
                addButton.setWidth(listBox.getPreferredWidth());
            } else {
                addButton.setHeight(listBox.getPreferredHeight());
            }
            this.setAddButton = false;
        }

        listBoxes.add(listBox);
        setupDragAndDrop(listBox);
        setupClickToDelete(listBox);

        // Add the new ListBox to the layout
        listBoxLayout.add(listBox);
        triggerOrderChange();
    }


    public void removeListBox(ListBox<T> listBox) {
        listBoxes.remove(listBox);
        listBoxLayout.remove(listBox);
        triggerOrderChange();
    }

    public void removeAllListBox() {
        listBoxes.clear();
        listBoxLayout.removeAll();
        triggerOrderChange();
    }

    public List<T> getData() {
        List<T> values = new ArrayList<>();
        for (ListBox<T> listBox : listBoxes) {
            values.add(listBox.getValue());
        }
        return values;
    }

    public void setOnOrderChangeListener(OnOrderChangeListener<T> listener) {
        this.orderChangeListener = listener;
    }

    public void setOnAddButtonClick(Runnable runnable) {
        // Custom logic for the add button event
        addButton.addClickListener(event -> runnable.run());
    }

    private void setupDragAndDrop(ListBox<T> listBox) {
        DragSource<ListBox<T>> dragSource = DragSource.create(listBox);
        dragSource.addDragStartListener(event -> listBox.getStyle().set("opacity", "0.5"));
        dragSource.addDragEndListener(event -> listBox.getStyle().set("opacity", "1"));

        DropTarget<ListBox<T>> dropTarget = DropTarget.create(listBox);
        dropTarget.addDropListener(event -> {
            ListBox<T> dragged = (ListBox<T>) event.getDragSourceComponent().orElse(null);
            if (dragged != null && dragged != listBox) {
                // Swap positions
                int draggedIndex = listBoxes.indexOf(dragged);
                int targetIndex = listBoxes.indexOf(listBox);

                listBoxes.remove(draggedIndex);
                listBoxes.add(targetIndex, dragged);

                // Re-render components
                listBoxLayout.removeAll();
                listBoxes.forEach(listBoxLayout::add);

                // Trigger order change callback
                triggerOrderChange();
            }
        });
    }

    private void setupClickToDelete(ListBox<T> listBox) {
        listBox.addClickListener(event -> {
            Dialog dialog = new Dialog();
            dialog.setHeaderTitle("是否删除该选项?");
            VerticalLayout layout = new VerticalLayout();
            layout.setPadding(false);
            Button yesButton = new Button("是", yesEvent -> {
                removeListBox(listBox);
                dialog.close(); // Close dialog after deletion
            });

            Button noButton = new Button("否", noEvent -> dialog.close());
            noButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

            // Add buttons to the dialog
            HorizontalLayout dialogButtonLayout = new HorizontalLayout(yesButton, noButton);
            layout.add(dialogButtonLayout);
            dialog.add(layout);

            // Open the dialog
            dialog.open();
        });
    }

    private void triggerOrderChange() {
        if (orderChangeListener != null) {
            orderChangeListener.onOrderChange(getData());
        }
    }
}
