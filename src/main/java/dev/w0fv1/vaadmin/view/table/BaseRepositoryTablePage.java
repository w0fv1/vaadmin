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
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.persistence.criteria.Predicate;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import com.vaadin.flow.data.provider.QuerySortOrder;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

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

    private final Map<String, GenericRepository.PredicateBuilder<E>> extPredicateBuilders = new HashMap<>();

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
    public void build() {
        presetPredicate();
        predicateManager.addAllPredicates(extPredicateBuilders);

        super.initialize(); // 构建 UI

        createDialog = buildCreateDialog();
        buildRepositoryActionColumn();

        add(createDialog);

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
                predicateManager.addAllPredicates(extPredicateBuilders);
                List<SortOrder> sortOrders = querySortOrders.stream().map(SortOrder::new).toList();


                List<E> entities = genericRepository.getPage(entityClass, offset, limit, predicateManager, sortOrders);
                List<T> result = entities.stream().map(this::convertToDto).collect(Collectors.toList());
                log.debug("加载 offset={} limit={} filter={} 条数：{}", offset, limit, filter, result.size());
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
                predicateManager.addAllPredicates(extPredicateBuilders);
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
    private void buildLikeSearchPredicate(String filter) {
        predicateManager.removePredicate("likeSearch");
        if (filter != null && !filter.isBlank()) {
            predicateManager.putPredicate("likeSearch", (cb, root, predicates) -> {
                List<Predicate> likes = new ArrayList<>();
                for (String field : super.getLikeSearchFieldNames()) {
                    likes.add(cb.like(root.get(field), "%" + filter + "%"));
                }
                if (!likes.isEmpty()) {
                    predicates.add(cb.or(likes.toArray(new Predicate[0])));
                }
            });
        }
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
    public void extPredicate(String key, GenericRepository.PredicateBuilder<E> predicateBuilder) {
        this.extPredicateBuilders.put(key, predicateBuilder);
        predicateManager.addAllPredicates(extPredicateBuilders);
        refresh();
    }

    public void onResetFilterEvent() {
        predicateManager.clearPredicates();
        presetPredicate();
        predicateManager.addAllPredicates(extPredicateBuilders);
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
