package dev.w0fv1.vaadmin.view.from.component;

import dev.w0fv1.vaadmin.GenericRepository;
import dev.w0fv1.vaadmin.entity.BaseManageEntity;
import dev.w0fv1.vaadmin.view.EntitySelectButton;
import dev.w0fv1.vaadmin.view.model.form.BaseFormModel;
import dev.w0fv1.vaadmin.view.model.form.FormEntityField;
import dev.w0fv1.vaadmin.view.model.form.FormField;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
public class SingleEntitySelectField<E extends BaseManageEntity<ID>, ID> extends BaseFormField<ID> {

    private final EntitySelectButton<E, ID> entitySelectButton;
    private Boolean isSingle;

    public SingleEntitySelectField(Field field, BaseFormModel formModel, GenericRepository genericRepository) {
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
        ID modelData = getModelData();

        if (modelData != null) {
            this.entitySelectButton.setValue(new ArrayList<>() {{
                add(modelData);
            }});
        }


        add(this.entitySelectButton);
    }

    @Override
    ID getData() {
        if (this.entitySelectButton.getValue().isEmpty()) {
            return null;
        }

        return this.entitySelectButton.getValue().getFirst();
    }


    @Override
    void setData(ID data) {
        this.entitySelectButton.setValue(new ArrayList<>() {{
            add(data);
        }});
    }

    @Override
    public void clear() {
        this.entitySelectButton.clear();
    }

}
