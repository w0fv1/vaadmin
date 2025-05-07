package dev.w0fv1.vaadmin.view.form.component;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import dev.w0fv1.vaadmin.view.form.model.BaseFormModel;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;

/**
 * FileUploadFieldComponent
 * 文件上传表单字段基类，绑定自定义的数据类型。
 *
 * 设计要求：
 * - 控件初始化放在 initStaticView；
 * - 内部持有数据；
 * - 数据与UI分离；
 * - 支持幂等pushViewData。
 */
@Slf4j
public abstract class BaseFileUploadFieldComponent<Type> extends BaseFormFieldComponent<Type> {

    private Upload upload;       // 上传控件
    private Button uploadButton; // 上传按钮
    private MemoryBuffer memoryBuffer; // 缓存上传文件

    private Type data; // 内部持有数据

    public BaseFileUploadFieldComponent(Field field, BaseFormModel formModel) {
        super(field, formModel);
    }

    @Override
    public void initStaticView() {
        this.memoryBuffer = new MemoryBuffer();
        this.uploadButton = new Button("上传" + this.getFormField().title());

        this.upload = new Upload(memoryBuffer);
        this.upload.setUploadButton(uploadButton);
        this.upload.setReceiver(memoryBuffer);
        this.upload.setWidthFull();
        this.upload.setEnabled(getFormField().enabled());

        this.upload.addSucceededListener(event -> {
            handleUploadSucceeded(memoryBuffer);
            pushViewData();
        });

        add(this.upload);
    }



    @Override
    public void pushViewData() {
        // 通常文件上传不需要根据数据刷新控件，所以这里可以留空或者根据需要处理
        // 可以根据data是否为空来决定界面提示，比如设置按钮文字
        if (uploadButton != null) {
            if (data != null) {
                uploadButton.setText("已上传文件 (点击重新上传)");
            } else {
                uploadButton.setText("上传" + this.getFormField().title());
            }
        }
    }

    @Override
    public Type getData() {
        return data;
    }

    @Override
    public void setData(Type data) {
        this.data = data;
    }

    @Override
    public void clearData() {
        this.data = null;
    }

    @Override
    public void clearUI() {
        if (upload != null) {
            upload.clearFileList();
            if (uploadButton != null) {
                uploadButton.setText("上传" + this.getFormField().title());
            }
        }
    }

    /**
     * 子类必须实现：处理上传成功后的文件。
     * 将 MemoryBuffer 转换为需要绑定的数据。
     */
    public abstract void handleUploadSucceeded(MemoryBuffer buffer);
}
