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
    private Dialog dialog;
    private String title;
    private EntitySelectPage<E, ID> selectPage;
    private List<ID> selectedItems = new ArrayList<>();

    private Class<E> entityType;
    private Boolean singleSelection;

    public EntitySelectButton(
            String title,
            Class<E> entityClass,
            Boolean singleSelection,
            GenericRepository genericRepository,
            Boolean enabled
    ) {
        this(title, entityClass, singleSelection, enabled);
        setGenericRepository(genericRepository);
    }

    public EntitySelectButton(
            String title,
            Class<E> entityClass,
            Boolean singleSelection,
            Boolean enabled
    ) {
        super(title);
        this.title = title;
        this.entityType = entityClass;
        this.singleSelection = singleSelection;
        this.setEnabled(enabled); // 直接根据enabled状态设置按钮是否允许输入
    }


    public void setGenericRepository(GenericRepository genericRepository) {
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
                this.entityType,
                onFinishCallback,
                this.singleSelection,
                genericRepository
        );

        dialog.add(selectPage);

        this.addClickListener(event -> {
            if (!isEnabled()) return; // 再次确认enabled状态
            dialog.open();
            selectPage.refresh();
            selectPage.setSelectedData(selectedItems);
        });

    }

    public void clear() {
        setText(title);
        selectPage.clear();
    }

    public List<ID> getValue() {
        return selectedItems;
    }

    public void setValue(List<ID> selectedItems) {
        // 判断 selectedItems 是否为空或包含不合法数据
        if (selectedItems == null || selectedItems.isEmpty()) {
            return; // 不设置
        }

        ID first = selectedItems.get(0);
        if ((first instanceof Number && ((Number) first).longValue() == 0L) ||
                (first instanceof String && ((String) first).isEmpty())) {
            return; // 无效值，退出
        }

        this.selectedItems.clear();
        this.selectedItems.addAll(selectedItems);
        if (selectPage != null) {
            selectPage.setSelectedData(selectedItems);
        }
        setText("ID为" + selectedItems + "的" + selectedItems.size() + "条数据(点击重选)");
    }


}
