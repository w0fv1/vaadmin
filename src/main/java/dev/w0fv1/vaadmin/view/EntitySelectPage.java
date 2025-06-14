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
import java.util.stream.Stream;

import static dev.w0fv1.vaadmin.util.TypeUtil.isBaseType;
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
    private final List<E> data = new ArrayList<>();

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
    private final Set<String> permanentPredicateKeys = new HashSet<>();          // >>> NEW

    public void clear() {
        data.clear();
        // Removed likeSearchInput.clear();
        idSearchInput.clear();
        uuidSearchInput.clear();
        predicateManager.clearPredicatesWithOut(
                Stream.concat(Stream.of("init"), permanentPredicateKeys.stream())
                        .toArray(String[]::new));
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
            if (!isBaseType(field.getType())) {
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

    public void initialize() {
        loadData();
        pushViewData();
    }

    /**
     * Loads data for the current page with applied filters.
     */
    public void loadData() {
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
            pushViewData();                    // >>> PATCH
            selectedCheckboxes.clear();        // 保证旧 checkbox 引用清空 // >>> PATCH
            lastSelectedCheckbox = null;       // >>> PATCH
        }
    }

    public void setData(List<E> data) {
        this.data.clear();
        if (data != null) {
            this.data.addAll(data);
        }
    }

    /**
     * 外部设置需要“被勾选”的 ID 列表。
     * - 同步 `selectedItems`；
     * - 清空旧的 checkbox 引用；
     * - 立即刷新 Grid，使对应行复选框显示为选中状态。
     */
    public void setSelectedData(List<ID> data) {
        log.debug("setSelectedData 被调用，参数: {}", data);

        // 1. 更新内部选中集合
        this.selectedItems.clear();
        if (data != null) {
            this.selectedItems.addAll(data);
        }

        // 2. 重置本地复选框引用，防止旧引用失效
        this.selectedCheckboxes.clear();
        this.lastSelectedCheckbox = null;

        // 3. **刷新 Grid** —— 重新渲染 ComponentColumn，使复选框选中状态与 selectedItems 对齐
        pushViewData();
        log.debug("setSelectedData 完成，已刷新 Grid 并同步选中状态");
    }

    public void pushViewData() {
        grid.setItems(this.data);
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
        predicateManager.clearPredicatesWithOut(
                Stream.concat(Stream.of("init"), permanentPredicateKeys.stream())
                        .toArray(String[]::new));
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

    /* -------------------- 外部新增永久过滤器 -------------------- */
    public void addPermanentFilter(String key, GenericRepository.PredicateBuilder<E> builder) {   // >>> NEW
        if (builder == null) {
            return;
        }
        predicateManager.putPredicate(key, builder);
        permanentPredicateKeys.add(key);
        log.debug("已添加永久过滤器 key={}", key);
    }
}
