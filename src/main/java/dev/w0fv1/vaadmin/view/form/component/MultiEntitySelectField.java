package dev.w0fv1.vaadmin.view.form.component;

import dev.w0fv1.vaadmin.GenericRepository;
import dev.w0fv1.vaadmin.entity.BaseManageEntity;
import dev.w0fv1.vaadmin.view.EntitySelectButton;
import dev.w0fv1.vaadmin.view.model.form.BaseFormModel;
import dev.w0fv1.vaadmin.view.model.form.FormField;
import dev.w0fv1.vaadmin.view.model.form.FormEntitySelectField;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
public class MultiEntitySelectField<E extends BaseManageEntity<ID>, ID> extends BaseFormFieldComponent<List<ID>> {

    private final EntitySelectButton<E, ID> entitySelectButton;
    private final Boolean isSingle;

    public MultiEntitySelectField(Field field, BaseFormModel formModel, GenericRepository genericRepository) {
        super(field, formModel, false);

        FormField formField = field.getAnnotation(FormField.class);
        FormEntitySelectField formEntitySelectField = field.getAnnotation(FormEntitySelectField.class);
        String title = formField.title();
        isSingle = !field.getType().equals(List.class);
        this.entitySelectButton = new EntitySelectButton<>(
                "选择" + title,
                (Class<E>) formEntitySelectField.entityField().entityType(),
                isSingle,
                genericRepository

        );
        List<ID> modelData = getModelData();
        log.info("modelData is null :"+((modelData==null)?"null":modelData.toString()));
        if (modelData != null) {
            this.entitySelectButton.setValue(modelData);
        }
        add(this.entitySelectButton);
    }

    @Override
    public void initView() {
    }

    @Override
    public List<ID> getData() {
        return this.entitySelectButton.getValue();
    }


    @Override
    public void setData(List<ID> data) {
        this.entitySelectButton.setValue(new ArrayList<>() {{
            addAll(data);
        }});
    }

    @Override
    public void clearUI() {
        this.entitySelectButton.clear();
    }


}
