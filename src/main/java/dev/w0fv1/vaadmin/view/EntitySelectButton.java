package dev.w0fv1.vaadmin.view;


import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import dev.w0fv1.vaadmin.GenericRepository;
import dev.w0fv1.vaadmin.entity.BaseManageEntity;

import java.util.ArrayList;
import java.util.List;

public class EntitySelectButton<
        E extends BaseManageEntity<ID>,
        ID> extends Button {
    private final Dialog dialog;
    private final String title;
    private final EntitySelectPage<E, ID> selectPage;
    private final List<ID> selectedItems = new ArrayList<>();

    public EntitySelectButton(
            String title,
            Class<E> entityClass,
            Boolean singleSelection,
            GenericRepository genericRepository
    ) {
        super(title);
        this.title = title;
        // Implement the OnFinish callback
        this.dialog = new Dialog();

        EntitySelectPage.OnFinish<ID> onFinishCallback = selectedData -> {

            if (selectedData != null && !selectedData.isEmpty()) {
                selectedItems.clear();
                selectedItems.addAll(new ArrayList<>(selectedData));

                setText("ID为" + selectedItems + "的" + selectedData.size() + "条数据(点击重选)");
            } else {
                setText(title);
            }

            if (selectedData != null) {
                selectedData.forEach(data -> {
                    System.out.println("Selected Data: " + data.toString());
                });
            }
            dialog.close();
        };


        selectPage = new EntitySelectPage<>(
                entityClass,
                onFinishCallback,
                singleSelection, // Set to true for single selection
                genericRepository

        );

        dialog.add(selectPage);

        ComponentEventListener<ClickEvent<Button>> clickEventComponentEventListener = new ComponentEventListener<>() {
            @Override
            public void onComponentEvent(ClickEvent<Button> event) {
                dialog.open();
                selectPage.refresh();
                selectPage.setSelectedData(selectedItems);
            }
        };
        this.addClickListener(clickEventComponentEventListener);
    }

    public void clear() {
        setText(title);
        selectPage.clear();
    }

    public List<ID> getValue() {
        return selectedItems;
    }

    public void setValue(List<ID> selectedItems) {
        this.selectedItems.clear();
        this.selectedItems.addAll(selectedItems);
        selectPage.setSelectedData(selectedItems);
    }


}
