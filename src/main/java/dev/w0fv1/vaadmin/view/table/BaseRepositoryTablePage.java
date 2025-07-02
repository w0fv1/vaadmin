package dev.w0fv1.vaadmin.view.table;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import dev.w0fv1.vaadmin.GenericRepository;
import dev.w0fv1.vaadmin.GenericRepository.SortOrder;
import dev.w0fv1.vaadmin.entity.BaseManageEntity;
import dev.w0fv1.vaadmin.view.BasePage;
import dev.w0fv1.vaadmin.view.form.RepositoryForm;
import dev.w0fv1.vaadmin.view.form.model.BaseEntityFormModel;
import dev.w0fv1.vaadmin.view.table.model.BaseEntityTableModel;
import dev.w0fv1.vaadmin.view.table.model.TableField;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.query.criteria.JpaExpression;
import org.springframework.transaction.support.TransactionCallback;
import com.vaadin.flow.data.provider.QuerySortOrder;

import java.util.*;
import java.util.stream.Collectors;

import static dev.w0fv1.vaadmin.view.table.model.TableField.SqlType.JSONB;

@Slf4j
public abstract class BaseRepositoryTablePage<
        T extends BaseEntityTableModel<E, ID>,
        F extends BaseEntityFormModel<E, ID>,
        E extends BaseManageEntity<ID>,
        ID> extends BaseTablePage<T> implements BasePage {

    @Getter
    @Resource
    protected GenericRepository genericRepository;

    @Getter
    protected final GenericRepository.PredicateManager<E> predicateManager = new GenericRepository.PredicateManager<>();

    private final Map<String, GenericRepository.PredicateBuilder<E>> extendPredicateBuilders = new HashMap<>();

    private final Class<E> entityClass;
    private final Class<F> formClass;
    private final Class<T> tableClass;

    private Dialog createDialog;
    private RepositoryForm<F, E, ID> formInstance;

    private final F defaultFormModel;

    public BaseRepositoryTablePage(Class<T> tableClass, Class<F> formClass, Class<E> entityClass) {
        this(tableClass, formClass, null, entityClass);
    }

    public BaseRepositoryTablePage(Class<T> tableClass, F formModel, Class<E> entityClass) {
        this(tableClass, (Class<F>) formModel.getClass(), formModel, entityClass);
    }

    public BaseRepositoryTablePage(Class<T> tableClass, Class<F> formClass, F formModel, Class<E> entityClass) {
        super(tableClass);
        this.entityClass = entityClass;
        this.formClass = formClass;
        this.tableClass = tableClass;

        if (formModel == null) {
            try {
                formModel = formClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("无法实例化表单模型", e);
            }
        }
        this.defaultFormModel = formModel;
    }

    @PostConstruct
    private void afterInject() {
        createDialog = buildCreateDialog();
        add(createDialog);
    }

    @Override
    public void initialize() {
        presetPredicate();
        predicateManager.addAllPredicates(extendPredicateBuilders);
        super.initialize(); // 构建 UI
        buildRepositoryActionColumn();
        onBuild();
    }

    public void onBuild() {
        // 子类可实现页面构建后逻辑
    }

    public void onSave(ID id) {
        // 子类可在保存后处理
    }

    private Dialog buildCreateDialog() {
        Dialog dialog = new Dialog();
        try {
            formInstance = new RepositoryForm<>(
                    defaultFormModel,
                    id -> handleSave(id, dialog),
                    () -> handleCancel(dialog),
                    genericRepository
            );
            formInstance.initialize();
            dialog.add(new VerticalLayout(formInstance));
        } catch (Exception e) {
            throw new RuntimeException("无法创建 RepositoryForm 实例", e);
        }
        return dialog;
    }

    private void handleSave(ID id, Dialog dialog) {
        onSave(id);
        dialog.close();
        refresh(); // 触发刷新
    }

    private void handleCancel(Dialog dialog) {
        dialog.close();
        refresh();
    }

    public void buildRepositoryActionColumn() {
        if (enableUpdate()) {
            super.extendGridComponentColumn(this::createUpdateButton)
                    .setHeader("更新")
                    .setAutoWidth(true);
        }
    }

    private Component createUpdateButton(T t) {
        Button button = new Button("更新");
        button.addClickListener(event -> {
            Dialog updateDialog = new Dialog();
            RepositoryForm<F, E, ID> form = new RepositoryForm<>(
                    (F) t.toFormModel(),
                    id -> handleSave(id, updateDialog),
                    () -> handleCancel(updateDialog),
                    genericRepository
            );
            form.initialize();
            updateDialog.add(new VerticalLayout(form));
            add(updateDialog);
            updateDialog.open();
        });
        return button;
    }

    @Override
    public void onCreateEvent() {
        createDialog.open();
    }

    /**
     * 重构后的核心数据加载方法：分页 + 排序 + 模糊搜索。
     */
    @Override
    protected List<T> loadChunk(int offset, int limit, String filter, List<QuerySortOrder> querySortOrders) {
        return genericRepository.execute((TransactionCallback<List<T>>) status -> {
            try {
                buildLikeSearchPredicate(filter);
                predicateManager.addAllPredicates(extendPredicateBuilders);
                List<SortOrder> sortOrders = querySortOrders.stream().map(SortOrder::new).toList();

                int page = offset / limit;

                List<E> entities = genericRepository.getPage(entityClass, page, limit, predicateManager, sortOrders);
                List<T> result = entities.stream().map(this::convertToDto).collect(Collectors.toList());
                log.debug("加载 page={} limit={} filter={} 条数：{}", page, limit, filter, result.size());
                return result;
            } catch (Exception e) {
                status.setRollbackOnly();
                throw new RuntimeException("查询失败", e);
            }
        });
    }

    @Override
    protected Long getTotalSize(String filter) {
        return genericRepository.execute((TransactionCallback<Long>) status -> {
            try {
                buildLikeSearchPredicate(filter);
                predicateManager.addAllPredicates(extendPredicateBuilders);
                return genericRepository.getTotalSize(entityClass, predicateManager);
            } catch (Exception e) {
                status.setRollbackOnly();
                throw new RuntimeException("统计失败", e);
            }
        });
    }

    /**
     * 根据 filter 构建模糊搜索谓词
     */
    /**
     * 根据 filter 构建模糊搜索谓词
     *
     * <p>支持：</p>
     * <ul>
     *   <li>varchar / text 直接 ILIKE</li>
     *   <li>numeric / enum / date 统一 cast(... as varchar) ILIKE</li>
     *   <li>json / jsonb 字段使用 column::text ILIKE</li>
     * </ul>
     *
     * <p>可通过 {@link dev.w0fv1.vaadmin.view.table.model.TableField#sqlType()}
     * 显式指定 SQL 类型；若未指定，则自动根据 Java 类型推断，
     * 且 <b>List 类型默认视为 JSONB</b>。</p>
     */
    private void buildLikeSearchPredicate(String filter) {

        // 1. 先移除旧的 likeSearch 谓词，避免多次叠加
        predicateManager.removePredicate("likeSearch");

        // 2. 空串直接返回
        if (filter == null || filter.isBlank()) {
            return;
        }

        // 3. 统一处理成小写模糊匹配串
        final String lowerPattern = "%" + filter.toLowerCase() + "%";

        predicateManager.putPredicate("likeSearch", (cb, root, predicates) -> {

            List<Predicate> likes = new ArrayList<>();

            // 当前实体类型，用于反射读取字段注解
            Class<?> entityClass = root.getModel().getBindableJavaType();

            // 遍历所有开启 likeSearch 的字段
            for (String field : super.getLikeSearchFieldNames()) {

                Path<?> path = root.get(field);
                Class<?> javaType = path.getJavaType();

                /*---------------------------------- 读取 @TableField.sqlType() ----------------------------------*/
                TableField.SqlType sqlType = TableField.SqlType.AUTO;
                try {
                    java.lang.reflect.Field entityField = entityClass.getDeclaredField(field);
                    TableField tfAnno = entityField.getAnnotation(TableField.class);
                    if (tfAnno != null) {
                        sqlType = tfAnno.sqlType();
                    }
                } catch (NoSuchFieldException ignore) {
                }

                /*---------------------------------- AUTO 模式下：按 Java 类型推断 ----------------------------------*/
                if (sqlType == TableField.SqlType.AUTO) {
                    if (String.class.isAssignableFrom(javaType)) {
                        sqlType = TableField.SqlType.TEXT;
                    } else if (Number.class.isAssignableFrom(javaType)
                            || java.time.temporal.Temporal.class.isAssignableFrom(javaType)
                            || java.util.Date.class.isAssignableFrom(javaType)
                            || javaType.isEnum()) {
                        sqlType = TableField.SqlType.NUMERIC;
                    } else if (java.util.List.class.isAssignableFrom(javaType)) {
                        // List 默认当作 jsonb
                        sqlType = TableField.SqlType.JSONB;
                    } else {
                        // 兜底仍然按 TEXT 处理
                        sqlType = TableField.SqlType.TEXT;
                    }
                }

                /*---------------------------------- 根据 sqlType 生成表达式 ----------------------------------*/
                Expression<String> expr;
                switch (sqlType) {
                    case JSONB -> {
                        // json / jsonb 必须先 ::text 再 ILIKE
                        expr = cb.lower(cb.function("jsonb_pretty", String.class, path));
                    }
                    case NUMERIC ->                                // bigint/decimal/enum/date
                            expr = cb.lower(((JpaExpression<?>) path).cast(String.class));

                    default -> {
                        // 其他都让 Hibernate 自动 cast 为 varchar
                        expr = cb.lower(path.as(String.class));
                    }
                }

                likes.add(cb.like(expr, lowerPattern));
            }

            // 收敛成 OR
            if (!likes.isEmpty()) {
                predicates.add(cb.or(likes.toArray(new Predicate[0])));
            }
        });
    }


    private T convertToDto(E entity) {
        try {
            T dto = tableClass.getDeclaredConstructor().newInstance();
            dto.formEntity(entity);
            return dto;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("无法转换 DTO", e);
        }
    }

    /**
     * 扩展筛选器入口
     */
    public void extendPredicate(String key, GenericRepository.PredicateBuilder<E> predicateBuilder) {
        this.extendPredicateBuilders.put(key, predicateBuilder);
        predicateManager.addAllPredicates(extendPredicateBuilders);
        refresh();
    }

    public void onResetFilterEvent() {
        predicateManager.clearPredicates();
        presetPredicate();
        predicateManager.addAllPredicates(extendPredicateBuilders);
        refresh();
    }

    public void presetPredicate() {
        // 子类可注入默认 Predicate 逻辑
    }

    public void setDefaultFromModel(F defaultFromModel) {
        formInstance.setDefaultModel(defaultFromModel);
    }

    public Boolean enableUpdate() {
        return true;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        BasePage.super.beforeEnter(event);
    }
}
