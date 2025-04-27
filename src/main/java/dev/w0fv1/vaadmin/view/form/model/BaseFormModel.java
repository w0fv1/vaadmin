package dev.w0fv1.vaadmin.view.form.model;

import java.lang.reflect.Field;

public interface BaseFormModel {

    default <T extends BaseFormModel> T copy() {
        try {
            @SuppressWarnings("unchecked")
            T copyInstance = (T) this.getClass().getDeclaredConstructor().newInstance();

            Field[] fields = this.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                field.set(copyInstance, field.get(this));
            }

            return copyInstance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to copy form model", e);
        }
    }
}
