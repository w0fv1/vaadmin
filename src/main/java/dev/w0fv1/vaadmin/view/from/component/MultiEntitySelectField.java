package dev.w0fv1.vaadmin.view.from.component;

import dev.w0fv1.vaadmin.GenericRepository;
import dev.w0fv1.vaadmin.entity.BaseManageEntity;
import dev.w0fv1.vaadmin.view.EntitySelectButton;
import dev.w0fv1.vaadmin.view.model.form.BaseFormModel;
import dev.w0fv1.vaadmin.view.model.form.FormField;
import dev.w0fv1.vaadmin.view.model.form.FormEntityField;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.List;

@Slf4j
@Getter
public class MultiEntitySelectField<E extends BaseManageEntity<ID>, ID> extends BaseFormField<List<ID>> {

    private final EntitySelectButton<E, ID> entitySelectButton;
    private final Boolean isSingle;

    public MultiEntitySelectField(Field field, BaseFormModel formModel, GenericRepository genericRepository) {
        super(field, formModel);

        FormField formField = field.getAnnotation(FormField.class);
        FormEntityField formEntityField = field.getAnnotation(FormEntityField.class);
        String title = formField.title();
        isSingle = !field.getType().equals(List.class);
        this.entitySelectButton = new EntitySelectButton<>(
                "选择" + title,
                (Class<E>) formEntityField.entityType(),
                isSingle,
                genericRepository

        );
        List<ID> modelData = getModelData();
        if (modelData != null) {
            this.entitySelectButton.setValue(modelData);
        }
        add(this.entitySelectButton);
    }

    @Override
    List<ID> getData() {
        return this.entitySelectButton.getValue();
    }


    @Override
    void setData(List<ID> data) {

    }

    @Override
    public void clear() {

    }

}
