package dev.w0fv1.vaadmin.view.form.component;


import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import dev.w0fv1.vaadmin.view.model.form.BaseFormModel;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;


@Slf4j
public abstract class FileUploadFieldComponent extends BaseFormFieldComponent<String> {

    private final MemoryBuffer buffer = new MemoryBuffer();
    private Upload upload;
    private Button uploadButton;


    public FileUploadFieldComponent(Field field, BaseFormModel formModel) {
        super(field, formModel);
        uploadButton = new Button("上传" + this.getFormField().title());

        upload = new Upload(buffer);
        upload.setUploadButton(uploadButton);

        // 上传完成事件监听器
        upload.addSucceededListener(event -> {
            handleUploadSucceeded(this.buffer);
        });

        add(upload);
    }

    public abstract void handleUploadSucceeded(MemoryBuffer buffer);


    public abstract String getData();

    public abstract void setData(String data);

    public void clear() {
        this.upload.clearFileList();
    }
}
