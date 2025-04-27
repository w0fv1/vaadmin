package dev.w0fv1.vaadmin.test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.Route;
import dev.w0fv1.vaadmin.view.table.model.BaseTableModel;
import dev.w0fv1.vaadmin.view.table.model.TableConfig;
import dev.w0fv1.vaadmin.view.table.model.TableField;
import dev.w0fv1.vaadmin.view.table.BaseTablePage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 示例：使用 BaseTableManagementPage<MyEntity> + Mock 数据源
 */
@Slf4j
@Route("/sample/table")
public class SampleTablePage extends BaseTablePage<SampleTablePage.SampleData> {

    private final List<SampleData> originalData = new ArrayList<>();
    private List<SampleData> filteredData = new ArrayList<>();

    public SampleTablePage() {
        super(SampleData.class);
        initializeOriginalData();
        filteredData.addAll(originalData);
        build();
    }

    private void initializeOriginalData() {
        for (int i = 1; i <= getTotalSize(); i++) {
            originalData.add(new SampleData(i, "示例名称" + i, "描述内容示例" + i));
        }
    }

    @Override
    public List<SampleData> loadData(int page) {
        return filteredData.stream()
                .skip((long) page * getPageSize())
                .limit(getPageSize())
                .collect(Collectors.toList());
    }

    @Override
    public void onCreateEvent() {
        int newId = originalData.size() + 1;
        SampleData newData = new SampleData(newId, "新建名称" + newId, "新建描述内容" + newId);
        originalData.add(newData);
        filteredData.add(newData);
        super.reloadCurrentData();
        System.out.println("创建了一条新数据: " + newData.getName());
    }

    @Override
    public void onLikeSearchEvent(String value) {
        filteredData = originalData.stream()
                .filter(d -> d.getName().contains(value) || d.getDescription().contains(value))
                .collect(Collectors.toList());
        super.jumpPage(0);
        System.out.println("搜索关键字：" + value);
    }

    @Override
    public void onResetFilterEvent() {
        filteredData.clear();
        filteredData.addAll(originalData);
        super.jumpPage(0);
        System.out.println("过滤条件已重置");
    }

    @Override
    public Long getTotalSize() {
        return (long) originalData.size();
    }

    @Override
    public Component extendPrimaryAction() {
        return new Button("额外操作", VaadinIcon.PLUS.create(), e -> System.out.println("额外主操作"));
    }

    @Override
    public Component extendSubAction() {
        return new Button("子操作", VaadinIcon.COG.create(), e -> System.out.println("子操作触发"));
    }

    @Override
    public Component extendDataAction() {
        return new Button("数据额外操作", VaadinIcon.DATABASE.create(), e -> System.out.println("数据额外操作"));
    }

    @Override
    public void extendGridColumns() {
        extendGridColumn(SampleData::getDescription).setHeader("额外描述列");
    }

    @Override
    public Component extendPage() {
        Div extraDiv = new Div();
        extraDiv.setText("页面底部额外组件");
        extraDiv.getStyle().set("margin-top", "20px");
        return extraDiv;
    }

    @Getter
    @TableConfig(title = "示例数据管理", description = "这是一个完整的示例页面", likeSearch = true)
    public static class SampleData implements BaseTableModel {
        @TableField(displayName = "ID", order = 1)
        private final int id;

        @TableField(displayName = "名称", likeSearch = true, order = 2)
        private final String name;

        @TableField(displayName = "描述", likeSearch = true, order = 3)
        private final String description;

        public SampleData(int id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }
    }
}