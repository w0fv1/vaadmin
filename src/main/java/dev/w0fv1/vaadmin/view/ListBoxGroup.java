package dev.w0fv1.vaadmin.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dnd.DragSource;
import com.vaadin.flow.component.dnd.DropTarget;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class ListBoxGroup<T> extends VerticalLayout {

    public interface OnOrderChangeListener<T> {
        void onOrderChange(List<T> newOrder);
    }
    @Getter
    public static class ListBox<T> extends Div {
        private final T value;

        public ListBox(T value, String title) {
            this(value, title, null); // 调用带图片 URL 的构造函数，默认图片为 null
        }

        public ListBox(T value, String title, String imageUrl) {
            this.value = value;

            // 设置样式
            getStyle()
                    .set("display", "flex")
                    .set("align-items", "center")
                    .set("justify-content", "center")
                    .set("width", "150px") // 宽度
                    .set("height", "100px") // 高度
                    .set("border", "1px solid #ccc")
                    .set("border-radius", "8px")
                    .set("cursor", "pointer")
                    .set("position", "relative")
                    .set("overflow", "hidden");

            // 根据是否提供图片设置背景
            if (imageUrl != null && !imageUrl.isEmpty()) {
                getStyle()
                        .set("background-image", "url('" + imageUrl + "')")
                        .set("background-size", "cover")
                        .set("background-position", "center");
            } else {
                // 无图片时设置默认背景颜色
                getStyle()
                        .set("background-color", "#f0f0f0")
                        .set("color", "#555"); // 默认文字颜色
            }

            // 添加标题
            Div titleDiv = new Div();
            titleDiv.setText(title);
            titleDiv.getStyle()
                    .set("position", "absolute")
                    .set("bottom", "10px")
                    .set("left", "10px")
                    .set("color", imageUrl != null ? "white" : "#000") // 根据背景调整文字颜色
                    .set("background", imageUrl != null ? "rgba(0, 0, 0, 0.5)" : "transparent") // 半透明背景
                    .set("padding", "4px 8px")
                    .set("border-radius", "4px")
                    .set("font-size", "14px")
                    .set("overflow", "hidden")
                    .set("font-weight", "bold");
            titleDiv.setMaxWidth("114px");

            add(titleDiv);
        }
    }


    private final List<ListBox<T>> listBoxes = new ArrayList<>();
    private final Button addButton;
    private final HorizontalLayout listBoxLayout;
    private final HorizontalLayout layout;
    private OnOrderChangeListener<T> orderChangeListener;

    public ListBoxGroup() {
        setSpacing(false);
        setPadding(false);

        listBoxLayout = new HorizontalLayout();
        listBoxLayout.setSpacing(true);

        layout = new HorizontalLayout();
        layout.setSpacing(true);

        // Add the "+" button
        addButton = new Button("+");
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.setHeight("100px");
        layout.add(addButton);


        layout.add(listBoxLayout);

        Scroller scroller = new Scroller();
        scroller.setScrollDirection(Scroller.ScrollDirection.HORIZONTAL);
        scroller.setContent(layout);
        scroller.setMaxWidth("100%");

        add(scroller);
    }

    public void addListBox(ListBox<T> listBox) {
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
