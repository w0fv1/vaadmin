package dev.w0fv1.vaadmin.view.from;

import com.vaadin.flow.component.notification.NotificationVariant;
import dev.w0fv1.mapper.Mapper;
import dev.w0fv1.vaadmin.GenericRepository;
import dev.w0fv1.vaadmin.entity.BaseManageEntity;
import dev.w0fv1.vaadmin.view.framework.BaseMainView;
import dev.w0fv1.vaadmin.view.from.component.BaseFormField;
import dev.w0fv1.vaadmin.view.from.component.MultiEntitySelectField;
import dev.w0fv1.vaadmin.view.from.component.SingleEntitySelectField;
import dev.w0fv1.vaadmin.view.model.form.BaseEntityFormModel;
import dev.w0fv1.vaadmin.view.model.form.FormField;
import dev.w0fv1.vaadmin.view.model.form.FormEntityField;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;


@Slf4j
public class RepositoryForm<
        F extends BaseEntityFormModel<E, ID>,
        E extends BaseManageEntity<ID>,
        ID> extends BaseForm<F> {

    private final Class<E> entityClass;

    private final GenericRepository genericRepository;
    private final Runnable onCancel;
    private final Runnable onSave;


    private final Boolean isUpdate;

    public RepositoryForm(F fromModel, Runnable onSave, Runnable onCancel, GenericRepository genericRepository) {
        super(fromModel, fromModel.getId() != null);
        this.isUpdate = fromModel.getId() != null;
        this.genericRepository = genericRepository;
        this.entityClass = fromModel.getEntityClass();
        this.onCancel = onCancel;
        this.onSave = onSave;
        super.build();
    }

    public RepositoryForm(Class<F> fromClass, Runnable onSave, Runnable onCancel, GenericRepository genericRepository) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        this(fromClass.getDeclaredConstructor().newInstance(), onSave, onCancel, genericRepository);
    }

    @Override
    public void onCancel() {
        onCancel.run();
    }

    @Override
    BaseFormField<?> extMapComponent(Field field, F formModel) {

        if (field.isAnnotationPresent(FormEntityField.class) && Collection.class.isAssignableFrom(field.getType())) {
            return new MultiEntitySelectField<>(field, formModel, genericRepository);
        } else if (field.isAnnotationPresent(FormEntityField.class) && !Collection.class.isAssignableFrom(field.getType())) {
            return new SingleEntitySelectField<>(field, formModel, genericRepository);
        }
        return null;
    }


    @Override
    public void onSave(F fromModel) {

        log.info(fromModel.toString());

        E model = genericRepository.execute(status -> {
            E saveModel = null;
            try {
                if (this.isUpdate) {
                    saveModel = genericRepository.find(fromModel.getId(), entityClass);
                    fromModel.translate(saveModel);
                } else {
                    saveModel = fromModel.toEntity();
                }
                for (Field declaredField : fromModel.getClass().getDeclaredFields()) {
                    if (!declaredField.isAnnotationPresent(FormField.class)) {
                        continue;
                    }
                    FormField fromField = declaredField.getAnnotation(FormField.class);


                    if (!declaredField.isAnnotationPresent(FormEntityField.class)) {
                        continue;
                    }
                    FormEntityField fromEntityField = declaredField.getAnnotation(FormEntityField.class);

                    Class<? extends BaseManageEntity<?>> entityClass = fromEntityField.entityType();
                    Mapper mapper = fromEntityField.entityMapper().getDeclaredConstructor().newInstance();

                    declaredField.setAccessible(true);

                    if (declaredField.getType().equals(List.class)) {
                        List<ID> ids = (List<ID>) declaredField.get(fromModel);
                        List entities = genericRepository.findAll(ids, entityClass);
                        mapper.accept(saveModel, entities);
                    } else {
                        ID id = (ID) declaredField.get(fromModel);
                        if (id == null && fromField.nullable()) {
                            mapper.accept(saveModel, null);
                            continue;
                        }
                        if (id == null) {
                            throw new RuntimeException("id == null && !fromField.nullable()");
                        }
                        Object entity = genericRepository.find(id, entityClass);
                        mapper.accept(saveModel, entity);
                    }
                }
                saveModel = genericRepository.save(saveModel);

            } catch (Exception e) {
                // 回滚事务
                status.setRollbackOnly();
                e.printStackTrace();
            }
            return saveModel;
        });
        BaseMainView.showNotification("保存成功！", NotificationVariant.LUMO_SUCCESS);
        onSave.run();
    }


}
