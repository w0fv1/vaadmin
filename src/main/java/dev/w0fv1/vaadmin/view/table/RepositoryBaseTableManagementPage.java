package dev.w0fv1.vaadmin.view.table;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.function.ValueProvider;
import dev.w0fv1.vaadmin.GenericRepository;
import dev.w0fv1.vaadmin.entity.BaseManageEntity;
import dev.w0fv1.vaadmin.view.form.RepositoryForm;
import dev.w0fv1.vaadmin.view.model.form.BaseEntityFormModel;
import dev.w0fv1.vaadmin.view.model.table.BaseEntityTableModel;
import jakarta.annotation.Resource;
import jakarta.persistence.criteria.Predicate;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;


import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;


@Slf4j
public abstract class RepositoryBaseTableManagementPage<
        T extends BaseEntityTableModel<E, ID>,
        F extends BaseEntityFormModel<E, ID>,
        E extends BaseManageEntity<ID>,
        ID> extends BaseTableManagementPage<T> {


    @Getter
    @Resource
    protected GenericRepository genericRepository;

    @Getter
    protected final GenericRepository.PredicateManager<E> predicateManager = new GenericRepository.PredicateManager<>();

    private final Class<E> entityClass;
    private final Class<F> formClass;
    private final Class<T> tableClass;

    private Dialog createDialog;

    abstract public void onSave(ID id);


    public interface CreateFromBuilder<
            F extends BaseEntityFormModel<E, ID>,
            E extends BaseManageEntity<ID>,
            ID> {
        RepositoryForm<F, E, ID> createForm();
    }

    private CreateFromBuilder createFromBuilder;

    public RepositoryBaseTableManagementPage(Class<T> tableClass, Class<F> formClass, Class<E> entityClass) {
        super(tableClass);
        this.entityClass = entityClass;
        this.formClass = formClass;
        this.tableClass = tableClass;

        super.build();
    }

    public RepositoryBaseTableManagementPage(Class<T> tableClass, Class<F> formClass, CreateFromBuilder<F, E, ID> createFromBuilder, Class<E> entityClass) {
        super(tableClass);
        this.entityClass = entityClass;
        this.formClass = formClass;
        this.tableClass = tableClass;
        this.createFromBuilder = createFromBuilder;
        super.build();
    }

    @Override
    public void onInit() {
        if (createFromBuilder == null) {
            var that = this;

            createFromBuilder = () -> {
                try {
                    return new RepositoryForm<>(
                            that.formClass,
                            (ID id) -> {
                                that.onSave(id);
                                createDialog.close();
                                reloadCurrentData();
                            },
                            () -> {
                                createDialog.close();
                                reloadCurrentData();
                            },
                            that.genericRepository
                    );
                } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                         IllegalAccessException e) {
                    throw new RuntimeException("无法创建 RepositoryForm 实例", e);
                }
            };
        }

        buildRepositoryActionColumn();
        createDialog = new Dialog();

        try {
            RepositoryForm<F, E, ID> formInstance = createFromBuilder.createForm();
            if (formInstance != null) {
                VerticalLayout dialogLayout = new VerticalLayout(formInstance);
                createDialog.add(dialogLayout);
            }
        } catch (Exception e) {
            throw new RuntimeException("无法创建 RepositoryForm 实例", e);
        }

        add(createDialog);
    }


    @Override
    public List<T> loadData(int page) {
        List<T> dataList = genericRepository.execute(new TransactionCallback<List<T>>() {

            @Override
            public List<T> doInTransaction(TransactionStatus status) {

                List<T> list = null;
                try {
                    list = genericRepository.getPage(entityClass, page, getPageSize(), predicateManager)
                            .stream()
                            .map((v) -> {
                                T dto = null;
                                try {
                                    dto = tableClass.getDeclaredConstructor().newInstance();
                                } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                                         NoSuchMethodException e) {
                                    throw new RuntimeException(e);
                                }
                                // 调用 fromModel 方法初始化数据
                                dto.formEntity(v);
                                return dto;
                            })
                            .toList();
                } catch (Exception e) {
                    // 回滚事务
                    status.setRollbackOnly();
                    throw new RuntimeException("事务执行失败，已回滚。", e);
                }
                return list;
            }
        });
        return dataList;
    }

    private static final String LIKE_SEARCH_KEY = "likeSearch";

    @Override
    public void onLikeSearch(String value) {

        getPredicateManager().removePredicate(LIKE_SEARCH_KEY);

        getPredicateManager().putPredicate(LIKE_SEARCH_KEY, (cb, root, predicates) -> {
            List<Predicate> likePredicates = new ArrayList<>();
            for (String s : super.getLikeSearchFieldName()) {
                likePredicates.add(cb.like(root.get(s), "%" + value + "%"));
            }
            if (!likePredicates.isEmpty()) {
                // Combine all LIKE predicates using OR
                predicates.add(cb.or(likePredicates.toArray(new Predicate[0])));
            }
        });
        // Jump to the first page
        super.jumpPage(0);
    }

    public void buildRepositoryActionColumn() {

        super.extComponentColumn(
                (ValueProvider<T, Component>) t -> {

                    Button button = new Button("更新");

                    button.addClickListener((ComponentEventListener<ClickEvent<Button>>) event -> {
                        Dialog updateDialog = new Dialog();
                        VerticalLayout dialogLayout = null;
                        dialogLayout = new RepositoryForm<>(
                                t.toFormModel(),
                                (ID id) -> {
                                    updateDialog.close();
                                    reloadCurrentData();
                                }, () -> {
                            updateDialog.close();
                            reloadCurrentData();
                        }, genericRepository
                        );
                        updateDialog.add(dialogLayout);
                        add(updateDialog);
                        updateDialog.open();
                    });

                    return button;
                }

        ).setHeader("更新").setAutoWidth(true);
    }

    @Override
    public void onCreate() {
        createDialog.open();
    }

    @Override
    public void onResetFilter() {
        predicateManager.clearPredicates();
    }

    @Override
    public Long getTotalSize() {
        return genericRepository.getTotalSize(entityClass, predicateManager);
    }

}
