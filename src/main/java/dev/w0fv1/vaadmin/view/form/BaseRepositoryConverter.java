package dev.w0fv1.vaadmin.view.form;

import dev.w0fv1.vaadmin.GenericRepository;

public interface BaseRepositoryConverter<Original, Entity> {
    Entity convert(GenericRepository genericRepository, Original original);


}
