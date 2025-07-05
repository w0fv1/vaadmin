package dev.w0fv1.vaadmin.view.table;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.*;
import com.vaadin.flow.function.ValueProvider;
import dev.w0fv1.vaadmin.view.table.component.BaseFieldComponent;
import dev.w0fv1.vaadmin.view.table.component.TextTableFieldComponent;
import dev.w0fv1.vaadmin.view.table.model.BaseTableModel;
import dev.w0fv1.vaadmin.view.table.model.TableConfig;
import dev.w0fv1.vaadmin.view.table.model.TableField;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.reflections.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dev.w0fv1.vaadmin.util.JsonUtil.toPrettyJson;
import static java.lang.reflect.Modifier.PRIVATE;
import static org.reflections.ReflectionUtils.getAllFields;

@Slf4j
public abstract class BaseTablePage<T extends BaseTableModel> extends VerticalLayout {

    private final Class<T> tableClass;
    private final TableConfig tableConfig;

    protected final Grid<T> grid = new Grid<>();
    protected final TextField likeSearchInput = new TextField();

    protected ConfigurableFilterDataProvider<T, Void, String> provider;

    private boolean staticViewBuilt = false;
    private boolean dataInitialized = false;


    @Getter
    private HorizontalLayout titleBar;
    @Getter
    private HorizontalLayout secondaryAction;
    @Getter
    private HorizontalLayout dataActions;
    @Getter
    private Component primaryActions;

    public BaseTablePage(Class<T> tableClass) {
        this.tableClass = tableClass;
        this.tableConfig = tableClass.getAnnotation(TableConfig.class);
        if (tableConfig == null) throw new IllegalStateException("@TableConfig not found");
    }

    // ================ 拆解后的生命周期方法 ================ //

    /**
     * 1. 初始化静态UI结构（不含数据）
     */
    public void initStaticView() {
        if (staticViewBuilt) return;

        buildTitleBar();
        buildSecondaryAction();
        buildLikeSearchBar();
        buildDataActions();
        buildGridColumns();

        add(grid);
        add(extendPage());

        staticViewBuilt = true;
    }

    /**
     * 2. 初始化数据组件，必须调用super.initData()
     */
    public void initData() {
        if (dataInitialized) return;


        grid.setPageSize(tableConfig.pageSize());   // ← 关键

        if (tableConfig.allRowsVisible()) {
            grid.setAllRowsVisible(true);
        } else {
            grid.setHeight("800px");
        }

        provider = DataProvider.fromFilteringCallbacks(this::fetch, this::count)
                // 使用默认 FilterCombiner，避免 NPE
                .withConfigurableFilter();
        grid.setItems(provider);

        dataInitialized = true;
    }

    /**
     * 3. 将数据推送至UI展示层，需幂等
     */
    public void pushViewData() {
        refresh();
    }

    public void refresh() {
        provider.refreshAll();
    }

    public void applyFilter(String keyword) {
        provider.setFilter(keyword == null || keyword.isBlank() ? null : keyword.trim());
        refresh();
    }


    /**
     * 4. 完整的初始化逻辑（子类控制调用时机）
     */
    public void initialize() {
        initStaticView();
        initData();
        pushViewData();
    }

    // ================ 原有的数据加载方法 ================ //

    private Stream<T> fetch(Query<T, String> q) {
        return loadChunk(q.getOffset(), q.getLimit(), q.getFilter().orElse(null), q.getSortOrders()).stream();
    }

    private int count(Query<T, String> q) {
        return getTotalSize(q.getFilter().orElse(null)).intValue();
    }

    // ================ 以下代码保留原有逻辑不变 ================ //

    private void buildGridColumns() {
        List<Field> fields = new ArrayList<>(getAllFields(tableClass, ReflectionUtils.withModifier(PRIVATE)).stream().toList());
        fields.sort(Comparator.comparingDouble(f -> {
            TableField tableField = f.getAnnotation(TableField.class);

            // 设置冻结列的排序值为最小（确保它排在最前面）
            if (tableField != null && tableField.frozen()) {
                return -1;  // 冻结列排最前面
            }

            // 非冻结列按原来的 order 排序
            return Optional.ofNullable(tableField).map(TableField::order).orElse(100);
        }));
        for (Field f : fields) {
            f.setAccessible(true);
            TableField tf = f.getAnnotation(TableField.class);
            String header = tf != null && !tf.displayName().isEmpty() ? tf.displayName() : f.getName();
            String columnKey = tf != null && !tf.key().isEmpty() ? tf.key() : f.getName();
            Grid.Column<T> col;  // <—— 把列句柄留下，后面统一处理冻结

            if (tf != null && tf.sortable()) {
                col = grid.addColumn(item -> getComparableFieldValue(item, f))
                        .setHeader(header).setSortable(true).setKey(columnKey).setAutoWidth(true);
            } else {
                col = grid.addComponentColumn(item -> buildSpanCell(item, f))
                        .setHeader(header).setKey(columnKey).setAutoWidth(true);
            }
            /* ---------- 新增逻辑：根据注解决定是否冻结 ---------- */
            if (tf != null && (tf.frozen() || tf.id())) {
                col.setFrozen(true);      // → 冻结到左边
                // 如需禁止用户拖动改变顺序，可再加：col.setReorderable(false);
            }
        }
        extendGridColumns();
        grid.addItemClickListener(e -> onItemClicked(e.getItem()));
        grid.addItemDoubleClickListener(e -> onItemDoubleClicked(e.getItem()));
        grid.setColumnReorderingAllowed(true);
    }

    private Component buildSpanCell(T item, Field f) {
        String val = getFieldStringValue(item, f, 25);
        Span s = new Span(val);
        s.getStyle().set("cursor", "pointer");
        s.addClickListener(ev -> onFieldClick(item, f, val));
        return s;
    }

    private Comparable<?> getComparableFieldValue(T item, Field f) {
        try {
            Object v = f.get(item);
            return v instanceof Comparable<?> c ? c : (v == null ? "" : v.toString());
        } catch (IllegalAccessException e) {
            return "";
        }
    }


    private void buildTitleBar() {
        primaryActions = extendPrimaryAction();
        titleBar = new HorizontalLayout(new H1(getTitle()), new Button(VaadinIcon.REFRESH.create(), v -> refresh()), primaryActions);
        titleBar.setAlignItems(Alignment.END);
        add(titleBar);
        if (!getDescription().isEmpty()) add(new Span(getDescription()));
    }

    private void buildSecondaryAction() {
        secondaryAction = new HorizontalLayout(extendSecondaryAction());
        add(secondaryAction);
    }

    /**
     * 构建模糊搜索栏：
     * - 在同一行（HorizontalLayout）中，最左侧放文字标识，紧接输入框，最后是搜索按钮。
     */
    private void buildLikeSearchBar() {
        if (!tableConfig.likeSearch()) return;

        // 创建布局，保证三元素同行显示
        HorizontalLayout likeSearchBar = new HorizontalLayout();
        likeSearchBar.setAlignItems(Alignment.CENTER);

        // 文字标识
        Span label = new Span("关键字搜索：");

        // 输入框
        likeSearchInput.setPlaceholder("搜索 " + getLikeSearchFieldNames());
        likeSearchInput.addValueChangeListener(e -> applyFilter(e.getValue())); // 原有即时过滤逻辑保留

        // 搜索按钮
        Button searchButton = new Button(VaadinIcon.SEARCH.create(), e -> applyFilter(likeSearchInput.getValue()));
        searchButton.getElement().setAttribute("title", "搜索");

        // 组装并添加进页面
        likeSearchBar.add(label, likeSearchInput, searchButton);
        add(likeSearchBar);
    }


    private void buildDataActions() {
        dataActions = new HorizontalLayout();
        dataActions.setWidthFull(); // 关键：让 HorizontalLayout 占满宽度

        if (enableCreate()) dataActions.add(new Button("创建", e -> onCreateEvent()));
        dataActions.add(extendDataAction());
        dataActions.setJustifyContentMode(JustifyContentMode.END);
        add(dataActions);
    }

    private String getFieldStringValue(T item, Field f, int max) {
        try {
            Object v = f.get(item);
            return v == null ? "-" : (v instanceof Map<?, ?> m ? toPrettyJson(m) : v.toString()).substring(0, Math.min(max, v.toString().length())) + (v.toString().length() > max ? "…" : "");
        } catch (IllegalAccessException e) {
            return "Error";
        }
    }

    public List<String> getLikeSearchFieldNames() {
        List<String> fieldNames = new ArrayList<>();
        Set<Field> fields = getAllFields(tableClass, ReflectionUtils.withModifier(PRIVATE));
        for (Field f : fields) {
            TableField annotation = f.getAnnotation(TableField.class);
            if (annotation != null && annotation.likeSearch()) {
                String key = annotation.key();
                if (key != null && !key.isBlank()) {
                    fieldNames.add(key);
                } else {
                    fieldNames.add(f.getName());
                }
            }
        }
        return fieldNames;
    }

    // ======= 抽象方法保留原接口 ======= //

    protected abstract List<T> loadChunk(int offset, int limit, String filter, List<QuerySortOrder> sortOrders);

    protected abstract Long getTotalSize(String filter);

    public abstract void onCreateEvent();

    // ======= 扩展点，保留原有方法 ======= //

    public String getTitle() {
        return tableConfig.title();
    }

    public String getDescription() {
        return Optional.ofNullable(tableConfig.description()).orElse("");
    }

    public Component extendPrimaryAction() {
        return new Div();
    }

    public Component extendSecondaryAction() {
        return new Div();
    }

    public Component extendDataAction() {
        return new Div();
    }

    public Component extendPage() {
        return new Div();
    }

    // ======= 组件添加扩展方法 ======= //

    /**
     * 向 titleBar 添加组件
     *
     * @param components 要添加的组件
     */
    public void addTitleBar(Component... components) {
        if (titleBar != null) {
            titleBar.add(components);
        }
    }

    /**
     * 向 secondaryAction 添加组件
     *
     * @param components 要添加的组件
     */
    public void addSecondaryAction(Component... components) {
        if (secondaryAction != null) {
            secondaryAction.add(components);
        }
    }

    /**
     * 向 dataActions 添加组件
     *
     * @param components 要添加的组件
     */
    public void addDataActions(Component... components) {
        if (dataActions != null) {
            dataActions.add(components);
        }
    }

    /**
     * 向 primaryActions 添加组件，仅当其是 Composite 类型容器时有效
     *
     * @param components 要添加的组件
     */
    public void addPrimaryActions(Component... components) {
        if (primaryActions instanceof HasComponents) {
            ((HasComponents) primaryActions).add(components);
        } else {
            log.warn("primaryActions 不是容器类型，不能添加组件: {}", primaryActions.getClass().getSimpleName());
        }
    }


    public void extendGridColumns() {
    }

    public Grid.Column<T> extendGridColumn(ValueProvider<T, ?> valueProvider) {
        return this.grid.addColumn(valueProvider);
    }

    public <V extends Component> Grid.Column<T> extendGridComponentColumn(ValueProvider<T, V> componentProvider) {
        return this.grid.addComponentColumn(componentProvider);
    }

    public void onItemClicked(T item) {
    }

    public void onItemDoubleClicked(T item) {
    }

    public void onFieldClick(T item, Field field, Object value) {
        // 判断是否有 @TableFieldComponent 注解且启用
        TableField tableField = field.getAnnotation(TableField.class);
        BaseFieldComponent<?> fieldComponent = null;

        // 默认只处理 String 类型
        if (value instanceof String || tableField == null) {
            fieldComponent = new TextTableFieldComponent(field, getFieldStringValue(item, field, 200000));
        }

        if (fieldComponent == null) {
            return;
        }

        Dialog dialog = new Dialog();

        String label = "";

        if (tableField != null && tableField.displayName() != null && !tableField.displayName().isEmpty()) {
            label = tableField.displayName();
        } else {
            label = field.getName();
        }

        dialog.setHeaderTitle("字段详情: " + label);
        dialog.setModal(true);
        dialog.setWidth("400px");

        dialog.add(fieldComponent);

        Button close = new Button("关闭", e -> dialog.close());
        dialog.getFooter().add(close);

        dialog.open();
    }

    public Boolean enableCreate() {
        return true;
    }
}
