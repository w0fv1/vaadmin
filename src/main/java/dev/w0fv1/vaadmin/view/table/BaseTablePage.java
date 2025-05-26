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

    private final Grid<T> grid = new Grid<>();
    private final TextField likeSearchInput = new TextField();

    private ConfigurableFilterDataProvider<T, Void, String> provider;

    private boolean viewBuilt = false;

    public BaseTablePage(Class<T> tableClass) {
        this.tableClass = tableClass;
        this.tableConfig = tableClass.getAnnotation(TableConfig.class);
        if (tableConfig == null) throw new IllegalStateException("@TableConfig not found");
    }

    public void initialize() {
        if (viewBuilt) return;
        buildStaticView();
        viewBuilt = true;
    }

    public void refresh() {
        provider.refreshAll();
    }

    public void applyFilter(String keyword) {
        provider.setFilter(keyword == null || keyword.isBlank() ? null : keyword.trim());
        refresh();
    }

    private void buildStaticView() {
        buildTitleBar();
        buildSubActions();
        buildLikeSearchBar();
        buildDataActions();

        buildGridColumns();
        add(grid);
        add(extendPage());

        provider = DataProvider.fromFilteringCallbacks(this::fetch, this::count).withConfigurableFilter(null);
        grid.setItems(provider);
    }

    private Stream<T> fetch(Query<T, String> q) {
        return loadChunk(q.getOffset(), q.getLimit(), q.getFilter().orElse(null), q.getSortOrders()).stream();
    }

    private int count(Query<T, String> q) {
        return getTotalSize(q.getFilter().orElse(null)).intValue();
    }

    private void buildGridColumns() {
        List<Field> fields = new ArrayList<>(getAllFields(tableClass, ReflectionUtils.withModifier(PRIVATE)).stream().toList());
        fields.sort(Comparator.comparingDouble(f -> Optional.ofNullable(f.getAnnotation(TableField.class)).map(TableField::order).orElse(100)));
        for (Field f : fields) {
            f.setAccessible(true);
            TableField tf = f.getAnnotation(TableField.class);
            String header = tf != null && !tf.displayName().isEmpty() ? tf.displayName() : f.getName();
            String columnKey = tf != null && !tf.key().isEmpty() ? tf.key() : f.getName();

            if (tf != null && tf.sortable()) {
                grid.addColumn(item -> getComparableFieldValue(item, f))
                        .setHeader(header).setSortable(true).setKey(columnKey).setAutoWidth(true);
            } else {
                grid.addComponentColumn(item -> buildSpanCell(item, f))
                        .setHeader(header).setKey(columnKey).setAutoWidth(true);
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
        HorizontalLayout hl = new HorizontalLayout(new H1(getTitle()), new Button(VaadinIcon.REFRESH.create(), v -> refresh()), extendPrimaryAction());
        hl.setAlignItems(Alignment.END);
        add(hl);
        if (!getDescription().isEmpty()) add(new Span(getDescription()));
    }

    private void buildSubActions() {
        add(new HorizontalLayout(buildSecondaryAction()));
    }

    private void buildLikeSearchBar() {
        if (!tableConfig.likeSearch()) return;
        likeSearchInput.setPlaceholder("搜索 " + getLikeSearchFieldNames());
        likeSearchInput.addValueChangeListener(e -> applyFilter(e.getValue()));
        add(likeSearchInput);
    }

    private void buildDataActions() {
        HorizontalLayout hl = new HorizontalLayout();
        if (enableCreate()) hl.add(new Button("创建", e -> onCreateEvent()));
        hl.add(extendDataAction());
        hl.setJustifyContentMode(JustifyContentMode.END);
        add(hl);
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
        return getAllFields(tableClass, ReflectionUtils.withModifier(PRIVATE)).stream()
                .filter(f -> Optional.ofNullable(f.getAnnotation(TableField.class)).map(TableField::likeSearch).orElse(false))
                .map(Field::getName).collect(Collectors.toList());
    }

    protected abstract List<T> loadChunk(int offset, int limit, String filter, List<QuerySortOrder> sortOrders);

    protected abstract Long getTotalSize(String filter);

    public abstract void onCreateEvent();

    public String getTitle() {
        return tableConfig.title();
    }

    public String getDescription() {
        return Optional.ofNullable(tableConfig.description()).orElse("");
    }

    public Component extendPrimaryAction() {
        return new Div();
    }

    public Component buildSecondaryAction() {
        return new Div();
    }

    public Component extendDataAction() {
        return new Div();
    }

    public Component extendPage() {
        return new Div();
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
