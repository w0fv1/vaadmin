package dev.w0fv1.vaadmin.view.table;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.function.ValueProvider;
import dev.w0fv1.vaadmin.view.model.table.BaseTableModel;
import dev.w0fv1.vaadmin.view.model.table.TableConfig;
import dev.w0fv1.vaadmin.view.model.table.TableField;
import lombok.extern.slf4j.Slf4j;
import org.reflections.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.lang.reflect.Modifier.PRIVATE;
import static org.reflections.ReflectionUtils.getAllFields;

@Slf4j
public abstract class BaseTablePage<T extends BaseTableModel> extends VerticalLayout {
    private Grid<T> grid;
    private final List<T> data = new ArrayList<>();

    private final Span pageInfo = new Span();
    private final NumberField pageInput = new NumberField();

    private final TableConfig tableConfig;

    private int page = 0;

    private final TextField likeSearchTextInput = new TextField();
    private final Class<T> tableClass;

    public BaseTablePage(Class<T> tableClass) {
        this.tableClass = tableClass;
        tableConfig = tableClass.getAnnotation(TableConfig.class);
        if (tableConfig == null) {
            throw new IllegalStateException("@TableConfig not found");
        }
    }


    public void build() {
        this.grid = new Grid<>();
        setData(loadData(page));
        buildPaginationComponent();

        buildTitleBar();
        buildSubActions();
        buildLikeSearchActions();
        buildDataActions();

        add(extendDataAction());

        buildDataGrid();
        add(grid);

        extendGridColumns();
        add(extendPage());

    }


    private void buildDataActions() {
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setWidth("100%");
        horizontalLayout.add(new Button("创建", v -> onCreateEvent()));
        horizontalLayout.setJustifyContentMode(JustifyContentMode.END);
        horizontalLayout.add(extendDataAction());
        add(horizontalLayout);
    }

    private void buildSubActions() {
        HorizontalLayout horizontalLayout = new HorizontalLayout();

        horizontalLayout.add(extendSubAction());

        add(horizontalLayout);
    }

    private void buildTitleBar() {
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.add(new H1(getTitle()));
        horizontalLayout.add(new Button(VaadinIcon.REFRESH.create(), (ComponentEventListener<ClickEvent<Button>>) v -> {
            refresh();
        }));
        horizontalLayout.setAlignItems(Alignment.END);
        horizontalLayout.add(extendPrimaryAction());
        add(horizontalLayout);
        if (getDescription() != null && !getDescription().isEmpty()) {
            add(new Span(getDescription()));
        }
    }


    public void refresh() {
        resetFilter();
        onResetFilterEvent();
        jumpPage(0);
    }

    abstract public List<T> loadData(int page);

    private void setData(List<T> data) {
        this.data.clear();
        this.data.addAll(data);
        this.grid.setItems(this.data);
    }

    public void nextPage() {
        if (page < getTotalPages() - 1) {
            page++;
            jumpPage(page);
        }
    }

    public void previousPage() {
        if (page > 0) {
            page--;
            jumpPage(page);
        }
    }

    public void jumpPage(int page) {
        this.page = page;
        setData(loadData(this.page));
        updatePageInfo();
    }

    public void reloadCurrentData() {
        jumpPage(page);
    }

    // Method to clear all filters
    public void resetFilter() {
        likeSearchTextInput.clear();
        jumpPage(0);

    }

    abstract public void onResetFilterEvent();

    private void buildPaginationComponent() {
        Button previousButton = new Button("上一页", e -> previousPage());
        Button nextButton = new Button("下一页", e -> nextPage());
        Button jumpButton = new Button("跳转", e -> {
            int targetPage = pageInput.getValue().intValue() - 1; // Convert to zero-indexed
            jumpPage(targetPage);
        });

        pageInput.setPlaceholder("页码");
        pageInput.setMin(1);
        pageInput.setMax(getTotalPages());
        pageInput.setStep(1);

        updatePageInfo(); // Initialize page info

        HorizontalLayout paginationLayout = new HorizontalLayout(
                previousButton, pageInfo, nextButton, pageInput, jumpButton
        );
        paginationLayout.setAlignItems(Alignment.CENTER);

        add(paginationLayout); // Add pagination layout to the view
    }

    private void updatePageInfo() {
        pageInfo.setText("第 " + (page + 1) + " 页，共 " + getTotalPages() + " 页");
    }


    public void refreshItem(T t) {
        grid.getDataProvider().refreshItem(t);
    }


    private void buildDataGrid() {
        List<Field> fieldList = getAllFields(tableClass, ReflectionUtils.withModifier(PRIVATE)).stream().toList();

        fieldList = fieldList.stream()
                .sorted(Comparator.comparingDouble(f -> {
                    TableField annotation = f.getAnnotation(TableField.class);
                    return annotation != null ? annotation.order() : 100;
                }))
                .toList();

        for (Field field : fieldList) {
            field.setAccessible(true); // Allow access to private fields
            TableField tableFieldInfo = field.getAnnotation(TableField.class);

            String displayName = field.getName();
            if (tableFieldInfo != null && !tableFieldInfo.displayName().isEmpty()) {
                displayName = tableFieldInfo.displayName();
            }
            grid.addColumn(data -> getFieldStringValue(data, field)) // Data provider
                    .setHeader(displayName) // Formatted header
                    .setAutoWidth(true)
                    .setResizable(true); // Enable sorting
        }
        grid.setWidthFull();
        grid.getStyle().set("border", "1px solid #ddd").set("padding", "10px");
        grid.setColumnReorderingAllowed(true); // Allow user to reorder columns
    }


    private void buildLikeSearchActions() {
        if (!tableConfig.likeSearch()) {
            return;
        }

        likeSearchTextInput.setLabel("模糊搜索/" + getLikeSearchFieldNameDisplayString());

        Button likeSearchButton = new Button("搜索", event -> onLikeSearchEvent(likeSearchTextInput.getValue()));
        HorizontalLayout searchLayout = new HorizontalLayout(likeSearchTextInput, likeSearchButton);
        searchLayout.setAlignItems(Alignment.END);
        add(searchLayout);
    }

    public String getLikeSearchFieldNameDisplayString() {
        List<Field> fieldList = getAllFields(tableClass, ReflectionUtils.withModifier(PRIVATE)).stream().toList();
        List<String> likeSearchFieldName = new ArrayList<>();
        for (Field field : fieldList) {
            if (!field.getType().equals(String.class)) {
                continue;
            }
            field.setAccessible(true); // Allow access to private fields
            TableField tableFieldInfo = field.getAnnotation(TableField.class);
            if (tableFieldInfo != null && tableFieldInfo.likeSearch()) {
                if (!tableFieldInfo.displayName().isEmpty()) {
                    likeSearchFieldName.add(tableFieldInfo.displayName());
                } else {
                    likeSearchFieldName.add(field.getName());
                }
            }
        }

        StringBuilder likeSearchFieldNameDisplayString = new StringBuilder();
        for (String s : likeSearchFieldName) {
            likeSearchFieldNameDisplayString.append(s);
            likeSearchFieldNameDisplayString.append("/");

        }
        return likeSearchFieldNameDisplayString.toString();
    }

    public abstract void onLikeSearchEvent(String value);

    // Helper method to safely retrieve field values using reflection
    private String getFieldStringValue(T data, Field field) {

        try {
            Object value = field.get(data); // Access field value
            if (value == null) {
                return "N/A";
            }
            String result = value.toString(); // Handle nulls
            if (result.length() > 20) {
                result = result.substring(0, 20) + "...";
            }
            return result;
        } catch (IllegalAccessException e) {
            return "Error"; // Handle access exceptions
        }
    }

    public List<String> getLikeSearchFieldName() {
        List<Field> fieldList = getAllFields(tableClass, ReflectionUtils.withModifier(PRIVATE)).stream().toList();
        List<String> likeSearchFieldName = new ArrayList<>();
        for (Field field : fieldList) {
            if (!field.getType().equals(String.class)) {
                continue;
            }
            field.setAccessible(true); // Allow access to private fields
            TableField tableFieldInfo = field.getAnnotation(TableField.class);
            if (tableFieldInfo != null && tableFieldInfo.likeSearch()) {
                if (!tableFieldInfo.likeSearchName().isEmpty()) {
                    likeSearchFieldName.add(tableFieldInfo.likeSearchName());
                } else {
                    likeSearchFieldName.add(field.getName());
                }
            }
        }
        return likeSearchFieldName;
    }


    public String getTitle() {
        return tableConfig.title();
    }

    public String getDescription() {
        return tableConfig.description();
    }

    public Component extendPrimaryAction() {
        return new Div();
    }

    public Component extendSubAction() {
        return new Div();
    }

    public Component extendDataAction() {
        return new Div();
    }

    abstract public void onCreateEvent();

    public Grid.Column<T> extendGridColumn(ValueProvider<T, ?> valueProvider) {
        return this.grid.addColumn(valueProvider);
    }

    public <V extends Component> Grid.Column<T> extendGridComponentColumn(ValueProvider<T, V> componentProvider) {
        return this.grid.addComponentColumn(componentProvider);
    }

    public void extendGridColumns() {
    }


    public Component extendPage() {
        return new Div();
    }


    abstract public Long getTotalSize();

    public int getPageSize() {
        return 10;
    }

    private int getTotalPages() {
        return (int) Math.ceil((double) getTotalSize() / getPageSize());
    }

}
