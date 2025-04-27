package dev.w0fv1.vaadmin.test;

import dev.w0fv1.vaadmin.GenericRepository;
import dev.w0fv1.vaadmin.view.form.RepositoryFieldMapper;

public class IdEchoRepositoryMapper implements RepositoryFieldMapper<Echo, EchoF> {

    @Override
    public void map(GenericRepository repository, Echo entity, EchoF form) {
        if (entity == null || form == null || form.getConvertEchoId() == null) {
            return;
        }
        Echo echo = repository.find(form.getConvertEchoId(), Echo.class);

        entity.setManyToOneEcho(echo);
    }
}
