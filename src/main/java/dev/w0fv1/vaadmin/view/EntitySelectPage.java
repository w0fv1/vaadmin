package dev.w0fv1.vaadmin.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import dev.w0fv1.vaadmin.GenericRepository;
import dev.w0fv1.vaadmin.view.table.model.TableField;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.reflections.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.*;

import static dev.w0fv1.vaadmin.util.TypeUtil.isBaseTye;
import static org.reflections.ReflectionUtils.getAllFields;

import dev.w0fv1.vaadmin.entity.BaseManageEntity;

@Slf4j
public class EntitySelectPage<
        E extends BaseManageEntity<ID>,
        ID> extends VerticalLayout {
    /**
     * Callback interface to return selected data upon completion.
     */
    public interface OnFinish<ID> {
        void onFinish(List<ID> selectedData);
    }

    private final GenericRepository genericRepository;

    private final Class<E> entityClass;
    private final OnFinish<ID> onFinish;
    private final boolean singleSelection;

    private final Grid<E> grid;
    private final List<ID> data = new ArrayList<>();

    // Removed likeSearchInput
    private final TextField idSearchInput = new TextField();
    private final TextField uuidSearchInput = new TextField();
    private final Button searchButton = new Button("搜索");
    private final Button resetButton = new Button("重置");
    private final Button finishButton = new Button("完成");
    private final Button cancelButton = new Button("取消");

    private final Span pageInfo = new Span();
    private final NumberField pageInput = new NumberField();

    private final GenericRepository.PredicateManager<E> predicateManager = new GenericRepository.PredicateManager<>();

    private int page = 0;
    private final int pageSize = 10;

    private final Set<ID> selectedItems = new HashSet<>();

    private final Set<Checkbox> selectedCheckboxes = new HashSet<>();

    private Checkbox lastSelectedCheckbox;

    public void clear() {
        data.clear();
        // Removed likeSearchInput.clear();
        idSearchInput.clear();
        uuidSearchInput.clear();
        predicateManager.clearPredicatesWithOut("init");
        selectedItems.clear();
        pageInput.clear();
        selectedCheckboxes.clear();
        lastSelectedCheckbox = null;
        jumpPage(0);
    }

    public EntitySelectPage(
            Class<E> entityClass,
            OnFinish<ID> onFinish,
            boolean singleSelection,
            GenericRepository genericRepository,
            GenericRepository.PredicateBuilder<E> builder
    ) {

        this(entityClass, onFinish, singleSelection, genericRepository);
        predicateManager.putPredicate("init", builder);
        applyFilters();
    }

    public EntitySelectPage(
            Class<E> entityClass,
            OnFinish<ID> onFinish,
            boolean singleSelection,
            GenericRepository genericRepository
    ) {

        this.entityClass = entityClass;
        this.onFinish = onFinish;
        this.singleSelection = singleSelection;
        this.genericRepository = genericRepository;

        this.grid = new Grid<>(entityClass, false);
        configureTitle();
        configureSearchFields();
        configureActionButtons();
        configureDataGrid();
        configurePaginationComponent();

        add(grid, createPaginationLayout(), createActionButtonLayout());
    }

    /**
     * Configures the title of the page.
     */
    private void configureTitle() {
        HorizontalLayout titleLayout = new HorizontalLayout();
        H1 title = new H1(getTitle());
        Button refreshButton = new Button(VaadinIcon.REFRESH.create(), event -> refresh());
        titleLayout.add(title, refreshButton);
        titleLayout.setAlignItems(Alignment.CENTER);
        add(titleLayout);
    }

    /**
     * Configures the search fields including ID and UUID exact search.
     */
    private void configureSearchFields() {
        // Removed likeSearchInput related configurations
        idSearchInput.setLabel("ID 精确搜索");
        uuidSearchInput.setLabel("UUID 精确搜索");

        searchButton.addClickListener(event -> applyFilters());
        resetButton.addClickListener(event -> resetFilters());

        HorizontalLayout searchLayout = new HorizontalLayout(idSearchInput, uuidSearchInput, searchButton, resetButton);
        searchLayout.setWidthFull();
        searchLayout.setSpacing(true);
        searchLayout.setAlignItems(Alignment.END);
        add(searchLayout);
    }

    /**
     * Configures the action buttons, primarily the 'Finish' button.
     */
    private void configureActionButtons() {
        finishButton.addClickListener(event -> {
            if (selectedItems.isEmpty()) {
                Dialog dialog = new Dialog(new Span("请至少选择一条数据。"));
                dialog.add(new Button("关闭", e -> dialog.close()));
                dialog.open();
                return;
            }
            onFinish.onFinish(new ArrayList<>(selectedItems));
        });
        cancelButton.addClickListener(event -> onFinish.onFinish(Collections.emptyList()));
    }

    /**
     * Creates the layout containing action buttons.
     */
    private HorizontalLayout createActionButtonLayout() {
        HorizontalLayout layout = new HorizontalLayout(finishButton, cancelButton);
        layout.setJustifyContentMode(JustifyContentMode.END);
        layout.setWidthFull();
        add(layout);
        return layout;
    }

    /**
     * Configures the data grid with selection and dynamic columns.
     */
    private void configureDataGrid() {
        grid.setWidthFull();
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.addClassName("entity-select-grid");
        grid.setColumnReorderingAllowed(true); // Allow user to reorder columns

        // Configure selection column
        grid.addComponentColumn(item -> {
            Checkbox checkbox = new Checkbox();
            checkbox.setValue(selectedItems.contains(item.getId()));

            checkbox.addValueChangeListener(event -> {
                if (event.getValue()) {
                    if (singleSelection) {
                        // Deselect the last selected checkbox
                        if (lastSelectedCheckbox != null && lastSelectedCheckbox != checkbox) {
                            lastSelectedCheckbox.setValue(false);
                            selectedItems.clear();
                        }
                        lastSelectedCheckbox = checkbox;
                    }
                    selectedItems.add(item.getId());
                    selectedCheckboxes.add(checkbox);
                } else {
                    selectedItems.remove(item.getId());
                    selectedCheckboxes.remove(checkbox);
                    if (lastSelectedCheckbox == checkbox) {
                        lastSelectedCheckbox = null;
                    }
                }
            });
            return checkbox;
        }).setHeader("选择").setAutoWidth(true);

        // Dynamically add columns based on entityClass fields
        List<Field> fieldList = new ArrayList<>(getAllFields(entityClass, ReflectionUtils.withModifier(java.lang.reflect.Modifier.PRIVATE)));
        fieldList.sort(Comparator.comparingInt(field -> {
            TableField annotation = field.getAnnotation(TableField.class);
            return annotation != null ? annotation.order() : 100;
        }));

        for (Field field : fieldList) {
            if (!isBaseTye(field.getType())) {
                continue;
            }

            field.setAccessible(true);
            TableField tableFieldInfo = field.getAnnotation(TableField.class);

            String displayName = (tableFieldInfo != null && !tableFieldInfo.displayName().isEmpty())
                    ? tableFieldInfo.displayName()
                    : field.getName();

            grid.addColumn(data -> getFieldStringValue(data, field))
                    .setHeader(displayName)
                    .setAutoWidth(true)
                    .setSortable(true);
        }

        // Load initial data
        loadData();
    }

    /**
     * Configures the pagination components.
     */
    private void configurePaginationComponent() {
        // This method is kept empty as pagination is handled separately
    }

    /**
     * Creates the pagination layout.
     */
    private HorizontalLayout createPaginationLayout() {
        Button previousButton = new Button("上一页", event -> previousPage());
        Button nextButton = new Button("下一页", event -> nextPage());
        Button jumpButton = new Button("跳转", event -> {
            Integer targetPage = pageInput.getValue() != null ? pageInput.getValue().intValue() - 1 : 0;
            jumpPage(targetPage);
        });

        pageInput.setPlaceholder("页码");
        pageInput.setMin(1);
        pageInput.setMax(getTotalPages());
        pageInput.setStep(1);
        pageInput.setWidth("100px");

        updatePageInfo();

        HorizontalLayout paginationLayout = new HorizontalLayout(previousButton, pageInfo, nextButton, pageInput, jumpButton);
        paginationLayout.setAlignItems(Alignment.CENTER);
        paginationLayout.setWidthFull();

        return paginationLayout;
    }

    /**
     * Updates the page information display.
     */
    private void updatePageInfo() {
        pageInfo.setText(String.format("第 %d 页，共 %d 页", page + 1, getTotalPages()));
    }

    /**
     * Loads data for the current page with applied filters.
     */
    private void loadData() {
        List<E> fetchedData = genericRepository.execute(status -> {
            List<E> list = new ArrayList<>();
            try {
                List<E> entities = genericRepository.getPage(entityClass, page, pageSize, predicateManager, null);
                list.addAll(entities);
            } catch (Exception e) {
                log.error("数据加载失败", e);
                status.setRollbackOnly();
                throw new RuntimeException("数据加载失败", e);
            }
            return list;
        });
        if (fetchedData != null) {
            setData(fetchedData);
        }
    }

    /**
     * Sets the data to the grid.
     */
    public void setData(List<E> data) {
        this.data.clear();
        // Assuming you want to display entities, not just their IDs
        // Adjust accordingly if you intend to store IDs only
        grid.setItems(data);
    }

    /**
     * Sets the data to the grid.
     */
    public void setSelectedData(List<ID> data) {
        this.selectedItems.clear();
        this.selectedItems.addAll(data);
        this.refresh();
    }

    /**
     * Refreshes the grid data.
     */
    public void refresh() {
        resetFilters();
        jumpPage(0);
    }

    /**
     * Navigates to the next page.
     */
    private void nextPage() {
        if (page < getTotalPages() - 1) {
            page++;
            jumpPage(page);
        }
    }

    /**
     * Navigates to the previous page.
     */
    private void previousPage() {
        if (page > 0) {
            page--;
            jumpPage(page);
        }
    }

    /**
     * Jumps to a specific page.
     */
    private void jumpPage(int targetPage) {
        if (targetPage < 0 || targetPage >= getTotalPages()) {
            return;
        }
        this.page = targetPage;
        loadData();
        updatePageInfo();
    }

    /**
     * Resets all filters and reloads data.
     */
    private void resetFilters() {
        // Removed likeSearchInput.clear();
        idSearchInput.clear();
        uuidSearchInput.clear();
        predicateManager.clearPredicatesWithOut("init");
        selectedItems.clear();
        jumpPage(0);
    }


    /**
     * Applies filters based on search inputs.
     */
    private void applyFilters() {

        // Removed like search filters

        // Apply ID exact search
        String idValue = idSearchInput.getValue();
        if (idValue != null && !idValue.trim().isEmpty()) {
            try {
                Long id = Long.parseLong(idValue.trim());
                predicateManager.putPredicate("idSearch", (cb, root, predicates) -> predicates.add(cb.equal(root.get("id"), id)));
            } catch (NumberFormatException e) {
                log.warn("Invalid ID format: {}", idValue);
            }
        }

        // Apply UUID exact search
        String uuidValue = uuidSearchInput.getValue();
        if (uuidValue != null && !uuidValue.trim().isEmpty()) {
            predicateManager.putPredicate("uuidSearch", (cb, root, predicates) -> predicates.add(cb.equal(root.get("uuid"), uuidValue.trim())));
        }

        // Reload data with new filters
        jumpPage(0);
    }


    /**
     * Retrieves the title from the table configuration.
     */
    private String getTitle() {
        String titleSuffix = singleSelection ? "(单选)" : "(多选)";
        return (entityClass.getSimpleName() + "选择数据") + titleSuffix;
    }

    /**
     * Safely retrieves the string value of a field from the data model.
     */
    private String getFieldStringValue(E data, Field field) {
        try {
            Object value = field.get(data);
            return value != null ? value.toString() : "N/A";
        } catch (IllegalAccessException e) {
            log.error("无法访问字段: {}", field.getName(), e);
            return "Error";
        }
    }

    private int getTotalPages() {
        long totalSize = genericRepository.getTotalSize(entityClass, predicateManager);
        return (int) Math.ceil((double) totalSize / pageSize);
    }

    @PostConstruct
    public void init() {
        // Initial data load is already handled in the constructor.
    }
}
