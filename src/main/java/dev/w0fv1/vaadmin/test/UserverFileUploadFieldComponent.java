package dev.w0fv1.vaadmin.test;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import dev.w0fv1.vaadmin.view.form.component.BaseFileUploadFieldComponent;
import dev.w0fv1.vaadmin.view.form.component.BaseFormFieldComponent;
import dev.w0fv1.vaadmin.view.form.component.CustomFormFieldComponentBuilder;
import dev.w0fv1.vaadmin.view.form.model.BaseFormModel;

import java.io.File;
import java.lang.reflect.Field;

import lombok.extern.slf4j.Slf4j;

import static dev.w0fv1.vaadmin.view.tools.Notifier.showNotification;

@Slf4j
public class UserverFileUploadFieldComponent extends BaseFileUploadFieldComponent<String> {

    private String url;
    private Span textField = new Span();   // 主界面只读文本框

    public UserverFileUploadFieldComponent(Field field, BaseFormModel formModel) {
        super(field, formModel);
            super.initialize();
    }


    @Override
    public void initStaticView() {
        super.initStaticView();
        textField.setId(getField().getName());
        textField.setWidthFull();
        add(textField);

    }

    @Override
    public void pushViewData() {
        super.pushViewData();
        if (getData() != null && !getData().isEmpty()) {
            // 更新UI组件显示上传文件名
            textField.setText("已上传文件: " + getData());
        } else {
            textField.setText("");
        }
    }

    public void handleUploadSucceeded(MemoryBuffer memoryBuffer) {
        log.info("handleUploadSucceeded :{}", memoryBuffer.getFileName());
        this.setData(memoryBuffer.getFileName());
        String mimeType = memoryBuffer.getFileData().getMimeType();



        this.url = mimeType;
    }

    public String getData() {
        return this.url;
    }

    public void setData(String data) {
        this.url = data;

    }

    @Override
    public void clearData() {
        this.url = null;
    }

    @Override
    public void clearUI() {
        this.url = null;

    }

    public static class UserverFileFormFieldComponentBuilder implements CustomFormFieldComponentBuilder {
        public UserverFileFormFieldComponentBuilder() {
        }

        public BaseFormFieldComponent<?> build(Field field, BaseFormModel formModel) {
            return new UserverFileUploadFieldComponent(field, formModel);
        }
    }
}
