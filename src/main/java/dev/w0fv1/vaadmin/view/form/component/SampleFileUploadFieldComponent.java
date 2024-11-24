package dev.w0fv1.vaadmin.view.form.component;


import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import dev.w0fv1.vaadmin.view.model.form.BaseFormModel;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;


@Slf4j
public class SampleFileUploadFieldComponent extends FileUploadFieldComponent {

    private String url;

    public SampleFileUploadFieldComponent(Field field, BaseFormModel formModel) {
        super(field, formModel);
    }

    public void handleUploadSucceeded(MemoryBuffer buffer) {
        log.info("handleUploadSucceeded :{}", buffer.getFileName());
        this.setData(buffer.getFileName());
    }

    @Override
    public String getData() {
        return url;
    }

    @Override
    public void setData(String data) {
        this.url = data;
    }

    @Override
    public void clear() {
        super.clear();
        this.url = null;
    }

    public static class SampleFileFormFieldComponentBuilder implements CustomFormFieldComponentBuilder {

        @Override
        public BaseFormFieldComponent<?> build(Field field, BaseFormModel formModel) {
            return new SampleFileUploadFieldComponent(field, formModel);
        }
    }
}
