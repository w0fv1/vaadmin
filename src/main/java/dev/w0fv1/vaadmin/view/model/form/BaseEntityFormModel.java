package dev.w0fv1.vaadmin.view.model.form;

import dev.w0fv1.vaadmin.entity.BaseManageEntity;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public interface BaseEntityFormModel<E extends BaseManageEntity<ID>, ID> extends BaseFormModel {
    ID getId();

    void setId(ID id);

    E toEntity();

    void translate(E model);


    default Class<E> getEntityClass() {
        try {
            Method method = this.getClass().getMethod("toEntity");
            Type returnType = method.getGenericReturnType();
            if (returnType instanceof ParameterizedType) {
                ParameterizedType pType = (ParameterizedType) returnType;
                Type actualType = pType.getActualTypeArguments()[0];
                if (actualType instanceof Class<?>) {
                    return (Class<E>) actualType;
                }
            } else if (returnType instanceof Class<?>) {
                return (Class<E>) returnType;
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("无法确定E的类型");
    }
}
