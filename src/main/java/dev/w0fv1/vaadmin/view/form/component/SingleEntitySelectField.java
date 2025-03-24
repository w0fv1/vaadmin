package dev.w0fv1.vaadmin.view.form.component;

import dev.w0fv1.vaadmin.GenericRepository;
import dev.w0fv1.vaadmin.entity.BaseManageEntity;
import dev.w0fv1.vaadmin.view.EntitySelectButton;
import dev.w0fv1.vaadmin.view.model.form.BaseFormModel;
import dev.w0fv1.vaadmin.view.model.form.FormEntitySelectField;
import dev.w0fv1.vaadmin.view.model.form.FormField;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
public class SingleEntitySelectField<E extends BaseManageEntity<ID>, ID> extends BaseFormFieldComponent<ID> {

    private final EntitySelectButton<E, ID> entitySelectButton;
    private Boolean isSingle;

    public SingleEntitySelectField(Field field, BaseFormModel formModel, GenericRepository genericRepository) {
        super(field, formModel, false);

        FormField formField = field.getAnnotation(FormField.class);
        FormEntitySelectField formEntitySelectField = field.getAnnotation(FormEntitySelectField.class);
        String title = formField.title();
        isSingle = !field.getType().equals(List.class);
        this.entitySelectButton = new EntitySelectButton<>(
                "选择" + title,
                (Class<E>) formEntitySelectField.entityField().entityType(),
                isSingle,
                genericRepository,
                formField.enabled()

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
    public void initView() {

    }

    @Override
    public ID getData() {
        if (this.entitySelectButton.getValue().isEmpty()) {
            return null;
        }

        return this.entitySelectButton.getValue().getFirst();
    }


    @Override
    public void setData(ID data) {
        this.entitySelectButton.setValue(new ArrayList<>() {{
            add(data);
        }});
    }

    @Override
    public void clearUI() {
        this.entitySelectButton.clear();
    }

}
