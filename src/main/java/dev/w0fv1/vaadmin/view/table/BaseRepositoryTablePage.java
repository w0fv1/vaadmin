package dev.w0fv1.vaadmin.view.table;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import dev.w0fv1.vaadmin.GenericRepository;
import dev.w0fv1.vaadmin.entity.BaseManageEntity;
import dev.w0fv1.vaadmin.view.BasePage;
import dev.w0fv1.vaadmin.view.form.RepositoryForm;
import dev.w0fv1.vaadmin.view.model.form.BaseEntityFormModel;
import dev.w0fv1.vaadmin.view.model.table.BaseEntityTableModel;
import jakarta.annotation.Resource;
import jakarta.persistence.criteria.Predicate;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.TransactionStatus;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

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

    private final Class<E> entityClass;
    private final Class<F> formClass;
    private final Class<T> tableClass;

    private Dialog createDialog;

    private F defaultFromModel;


    public void onSave(ID id) {

    }


    public BaseRepositoryTablePage(Class<T> tableClass, Class<F> formClass, Class<E> entityClass) {
        this(tableClass, formClass, null, entityClass);
    }

    public BaseRepositoryTablePage(Class<T> tableClass, Class<F> formClass,
                                   F formModel,
                                   Class<E> entityClass) {
        super(tableClass);

        this.entityClass = entityClass;
        this.formClass = formClass;
        this.tableClass = tableClass;

        if (formModel == null) {
            try {
                formModel = formClass.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

        this.defaultFromModel = formModel;

        super.buildView();
    }

    @Override
    public void onInitialized() {
        buildRepositoryActionColumn();
        createDialog = buildCreateDialog();
        add(createDialog);
    }

    RepositoryForm<F, E, ID> formInstance;


    private Dialog buildCreateDialog() {
        Dialog dialog = new Dialog();
        try {

            formInstance = new RepositoryForm<>(defaultFromModel,
                    id -> handleSave(id, createDialog),
                    () -> handleCancel(createDialog),
                    genericRepository);

            dialog.add(new VerticalLayout(formInstance));
        } catch (Exception e) {
            throw new RuntimeException("无法创建 RepositoryForm 实例", e);
        }
        return dialog;
    }


    private void handleSave(ID id, Dialog dialog) {
        onSave(id);
        dialog.close();
        reloadCurrentData();
    }

    private void handleCancel(Dialog dialog) {
        dialog.close();
        reloadCurrentData();
    }

    @Override
    public List<T> loadData(int page) {
        return genericRepository.execute(status -> fetchPageData(page, status));
    }

    private List<T> fetchPageData(int page, TransactionStatus status) {
        try {
            return genericRepository.getPage(entityClass, page, getPageSize(), predicateManager)
                    .stream()
                    .map(this::convertToDto)
                    .toList();
        } catch (Exception e) {
            status.setRollbackOnly();
            throw new RuntimeException("事务执行失败，已回滚。", e);
        }
    }

    private T convertToDto(E entity) {
        try {
            T dto = tableClass.getDeclaredConstructor().newInstance();
            dto.formEntity(entity);
            return dto;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onLikeSearchEvent(String value) {
        predicateManager.removePredicate("likeSearch");
        predicateManager.putPredicate("likeSearch", (cb, root, predicates) -> {
            List<Predicate> likes = new ArrayList<>();
            for (String field : super.getLikeSearchFieldName()) {
                likes.add(cb.like(root.get(field), "%" + value + "%"));
            }
            if (!likes.isEmpty()) {
                predicates.add(cb.or(likes.toArray(new Predicate[0])));
            }
        });
        super.jumpPage(0);
    }

    public void buildRepositoryActionColumn() {
        super.extendGridComponentColumn(this::createUpdateButton)
                .setHeader("更新").setAutoWidth(true);
    }

    private Component createUpdateButton(T t) {
        Button button = new Button("更新");
        button.addClickListener(event -> openUpdateDialog(t));
        return button;
    }

    private void openUpdateDialog(T t) {
        Dialog updateDialog = new Dialog();
        RepositoryForm<F, E, ID> form = new RepositoryForm<>(
                (F) t.toFormModel(),
                id -> handleSave(id, updateDialog),
                () -> handleCancel(updateDialog),
                genericRepository
        );
        updateDialog.add(new VerticalLayout(form));
        add(updateDialog);
        updateDialog.open();
    }

    @Override
    public void onCreateEvent() {
        createDialog.open();
    }

    @Override
    public void onResetFilterEvent() {
        predicateManager.clearPredicates();
    }

    @Override
    public Long getTotalSize() {
        return genericRepository.getTotalSize(entityClass, predicateManager);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        BasePage.super.beforeEnter(event);
    }

    @Override
    public void onGetUrlQueryParameters(ParameterMap parameters) {

    }

    @Override
    public void onGetPathParameters(ParameterMap parameters) {

    }

    public void setDefaultFromModel(F defaultFromModel) {
        formInstance.setDefaultModel(defaultFromModel);
    }

}