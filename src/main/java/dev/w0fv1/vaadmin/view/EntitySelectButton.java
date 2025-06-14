package dev.w0fv1.vaadmin.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import dev.w0fv1.vaadmin.GenericRepository;
import dev.w0fv1.vaadmin.entity.BaseManageEntity;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * EntitySelectButton
 * 实体选择按钮，支持单选/多选实体，并监听选中变化。
 */
@Slf4j
public class EntitySelectButton<
        E extends BaseManageEntity<ID>,
        ID> extends Button {

    private Dialog dialog;
    private String title;
    private EntitySelectPage<E, ID> selectPage;
    private List<ID> selectedItems = new ArrayList<>();

    private Class<E> entityType;
    private Boolean singleSelection;
    @Setter
    private Consumer<List<ID>> onValueChangeListener; // 监听器

    public EntitySelectButton(
            String title,
            Class<E> entityClass,
            Boolean singleSelection,
            GenericRepository genericRepository,
            Boolean enabled
    ) {
        this(title, entityClass, singleSelection, enabled);
        setGenericRepository(genericRepository,null);
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
        this.setEnabled(enabled);
    }

    public void setGenericRepository(GenericRepository genericRepository, GenericRepository.PredicateBuilder<E> predicateBuilder) {
        this.dialog = new Dialog();

        EntitySelectPage.OnFinish<ID> onFinishCallback = selectedData -> {
            if (selectedData != null && !selectedData.isEmpty()) {
                selectedItems.clear();
                selectedItems.addAll(new ArrayList<>(selectedData));
                setText("ID为" + selectedItems + "的" + selectedData.size() + "条数据(点击重选)");
            } else {
                selectedItems.clear();
                setText(title);
            }

            // 通知监听器
            if (onValueChangeListener != null) {
                onValueChangeListener.accept(new ArrayList<>(selectedItems));
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
            if (!isEnabled()) return;
            selectPage.setSelectedData(selectedItems);
            dialog.open();
        });
        selectPage.addPermanentFilter("preset", predicateBuilder);

        selectPage.initialize();
    }

    public void clear() {
        selectedItems.clear();
        setText(title);
        if (selectPage != null) {
            selectPage.clear();
        }
        if (onValueChangeListener != null) {
            onValueChangeListener.accept(new ArrayList<>(selectedItems));
        }
    }

    public List<ID> getValue() {
        return selectedItems;
    }

    public void setValue(List<ID> selectedItems) {

        if (selectedItems == null) {
            return;
        }

        if (selectedItems.isEmpty()) {
            return;
        }


        ID first = selectedItems.getFirst(); // 如果你用的是 Java 8 之前的版本，这里应改为 selectedItems.get(0)

        if ((first instanceof Number && ((Number) first).longValue() == 0L)) {
            return;
        }

        if ((first instanceof String && ((String) first).isEmpty())) {
            return;
        }


        this.selectedItems.clear();

        this.selectedItems.addAll(selectedItems);

        if (selectPage != null) {
            selectPage.setSelectedData(selectedItems);
        }

        String text = "ID为" + selectedItems + "的" + selectedItems.size() + "条数据(点击重选)";
        setText(text);

        // 主动通知监听器
        if (onValueChangeListener != null) {
            onValueChangeListener.accept(new ArrayList<>(this.selectedItems));
        }
    }

}
