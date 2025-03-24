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
public class SingleEntitySelectField<E extends BaseManageEntity<ID>, ID> extends BaseFormFieldComponent<ID> {

    private EntitySelectButton<E, ID> entitySelectButton;
    private Boolean isSingle;

    private FormField formField;
    private Field field;

    private final GenericRepository genericRepository;

    public SingleEntitySelectField(Field field, BaseFormModel formModel, GenericRepository genericRepository) {
        super(field, formModel, true);
        this.field = field;
        this.formField = field.getAnnotation(FormField.class);
        this.genericRepository = genericRepository;
        this.entitySelectButton.setGenericRepository(genericRepository);
    }


    @Override
    public void initView() {
        FormEntitySelectField formEntitySelectField = super.getField().getAnnotation(FormEntitySelectField.class);
        String title = super.getFormField().title();
        isSingle = !super.getField().getType().equals(List.class);
        this.entitySelectButton = new EntitySelectButton<>(
                "选择" + title,
                (Class<E>) formEntitySelectField.entityField().entityType(),
                isSingle,
                super.getFormField().enabled()
        );
        add(this.entitySelectButton);
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
