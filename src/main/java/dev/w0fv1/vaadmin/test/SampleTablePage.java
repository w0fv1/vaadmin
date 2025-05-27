package dev.w0fv1.vaadmin.test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.router.Route;
import dev.w0fv1.vaadmin.view.table.BaseTablePage;
import dev.w0fv1.vaadmin.view.table.model.BaseTableModel;
import dev.w0fv1.vaadmin.view.table.model.TableConfig;
import dev.w0fv1.vaadmin.view.table.model.TableField;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Route("/sample/table")
public class SampleTablePage extends BaseTablePage<SampleTablePage.SampleData> {

    // ------------------------------------------------------------------
    // 原始数据 + 模拟搜索用
    private final List<SampleData> originalData = new ArrayList<>();

    public SampleTablePage() {
        super(SampleData.class);
        // 初始化数据（100条）
        for (int i = 1; i <= 100; i++) {
            originalData.add(new SampleData(i, "示例名称" + i, "描述内容示例" + i));
        }
        // 构建 UI 并初始化数据加载器
        initialize();
    }

    // ==================================================================
    //                            数据统计
    // ==================================================================
    /**
     * 回调：获取总条数（可根据搜索关键字过滤）
     */
    @Override
    protected Long getTotalSize(String filter) {
        return originalData.stream()
                .filter(d -> filter == null
                        || d.getName().contains(filter)
                        || d.getDescription().contains(filter))
                .count();
    }

    // ==================================================================
    //                        “创建”按钮回调
    // ==================================================================
    /**
     * 点击“创建”按钮添加新数据
     */
    @Override
    public void onCreateEvent() {
        int newId = originalData.size() + 1;
        SampleData newData = new SampleData(newId, "新建名称" + newId, "新建描述内容" + newId);
        originalData.add(newData);
        refresh(); // 自动触发数据刷新
        log.info("创建新数据: {}", newData.getName());
    }

    // ==================================================================
    //                   主 / 子 / 数据 扩展动作示例
    // ==================================================================
    @Override
    public Component extendPrimaryAction() {
        return new Button("额外操作", VaadinIcon.PLUS.create(), e -> log.info("点击了额外主操作"));
    }

    @Override
    public Component extendSecondaryAction() {
        return new Button("子操作", VaadinIcon.COG.create(), e -> log.info("点击了子操作"));
    }

    @Override
    public Component extendDataAction() {
        return new Button("数据额外操作", VaadinIcon.DATABASE.create(), e -> log.info("点击了数据额外操作"));
    }

    // ==================================================================
    //                         Grid 扩展列
    // ==================================================================
    @Override
    public void extendGridColumns() {
        // 额外描述列（演示扩展）
        extendGridColumn(SampleData::getDescription)
                .setHeader("额外描述列")
                .setSortable(true)      // 同样支持排序
                .setKey("extra_desc");  // 显式设置 key 方便识别
    }

    // ==================================================================
    //                     核心：批量数据加载实现
    // ==================================================================
    /**
     * 加载数据块（Server-Side 排序 & 过滤）。
     *
     * @param offset     起始偏移
     * @param limit      数量
     * @param filter     搜索关键词，可 null
     * @param sortOrders Grid 排序字段与方向
     */
    @Override
    protected List<SampleData> loadChunk(int offset,
                                         int limit,
                                         String filter,
                                         List<QuerySortOrder> sortOrders) {

        log.debug("loadChunk offset={} limit={} filter={} sort={}", offset, limit, filter, sortOrders);

        /* 1. 先按搜索关键字过滤（LIKE 搜索） */
        Stream<SampleData> stream = originalData.stream()
                .filter(d -> filter == null
                        || d.getName().contains(filter)
                        || d.getDescription().contains(filter));

        /* 2. 再根据 sortOrders 进行多字段排序 */
        Comparator<SampleData> comparator = buildComparator(sortOrders);
        if (comparator != null) {
            stream = stream.sorted(comparator);
        }

        /* 3. 分页截取 */
        return stream
                .skip(offset)
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 根据 Grid 的 sortOrders 构造 Comparator，可支持多字段级联、ASC/DESC。
     * 如果前端未指定排序，则返回 null（保持原始顺序）。
     */
    private Comparator<SampleData> buildComparator(List<QuerySortOrder> sortOrders) {
        if (sortOrders == null || sortOrders.isEmpty()) {
            return null;
        }

        Comparator<SampleData> comparator = null;

        for (QuerySortOrder order : sortOrders) {
            String key = order.getSorted(); // 列 key
            SortDirection dir = order.getDirection();

            Comparator<SampleData> fieldComp = getFieldComparatorByKey(key, dir);

            if (fieldComp == null) continue; // 找不到则跳过

            comparator = (comparator == null) ? fieldComp : comparator.thenComparing(fieldComp);
        }

        return comparator;
    }

    /**
     * 针对指定列 key 生成 Comparator，默认支持 id/name/description/extra_desc；
     * 其他列可按需扩展。
     */
    private Comparator<SampleData> getFieldComparatorByKey(String key, SortDirection dir) {
        Comparator<SampleData> comp = null;

        switch (key) {
            case "id":
                comp = Comparator.comparingInt(SampleData::getId);
                break;
            case "name":
                comp = Comparator.comparing(SampleData::getName, Comparator.nullsFirst(String::compareTo));
                break;
            case "description":
            case "extra_desc":
                comp = Comparator.comparing(SampleData::getDescription, Comparator.nullsFirst(String::compareTo));
                break;
            default:
                // 兜底：反射取字段值（性能次之，但可覆盖所有字段）
                try {
                    Field f = SampleData.class.getDeclaredField(key);
                    f.setAccessible(true);
                    comp = Comparator.comparing(
                            (SampleData d) -> {
                                try {
                                    Object v = f.get(d);
                                    return v == null ? "" : v.toString();
                                } catch (IllegalAccessException e) {
                                    return "";
                                }
                            },
                            Comparator.nullsFirst(String::compareTo)
                    );
                } catch (NoSuchFieldException ignored) {
                }
        }

        if (comp == null) return null;
        return dir == SortDirection.DESCENDING ? comp.reversed() : comp;
    }

    // ==================================================================
    //                         页面底部扩展
    // ==================================================================
    @Override
    public Component extendPage() {
        Div footer = new Div();
        footer.setText("页面底部附加组件");
        footer.getStyle().set("margin-top", "20px");
        return footer;
    }

    // ==================================================================
    //                              数据模型
    // ==================================================================
    @Getter
    @TableConfig(title = "示例数据管理", description = "这是一个完整的示例页面", likeSearch = true)
    public static class SampleData implements BaseTableModel {
        @TableField(displayName = "ID", order = 1, sortable = true)
        private final int id;

        @TableField(displayName = "名称", likeSearch = true, order = 2, sortable = true)
        private final String name;

        @TableField(displayName = "描述", likeSearch = true, order = 3, sortable = true)
        private final String description;

        public SampleData(int id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }
    }
}
