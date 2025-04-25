package dev.w0fv1.vaadmin.view.form;

import dev.w0fv1.vaadmin.GenericRepository;
import dev.w0fv1.vaadmin.entity.BaseManageEntity;
import dev.w0fv1.vaadmin.view.model.form.BaseEntityFormModel;

public interface RepositoryFieldMapper<
        F extends BaseEntityFormModel<E, ?>,
        E extends BaseManageEntity<?>> {

    public void map(GenericRepository repository, E entity, F model);

}
