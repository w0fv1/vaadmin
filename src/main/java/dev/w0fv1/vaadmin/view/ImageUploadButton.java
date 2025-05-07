package dev.w0fv1.vaadmin.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Base64;

import static dev.w0fv1.vaadmin.view.tools.Notifier.showNotification;

public class ImageUploadButton<T> extends Div {

    private String initImageUrl;
    private final Div imageContainer;
    private final Button uploadButton;
    private final Button applyButton;       // 新增“应用”按钮
    private final Upload upload;

    // 用于暂存 handleUploadSucceeded 返回的结果
    private T uploadResult;

    public ImageUploadButton(String initImageUrl, ImageUploadHandler<T> handler) {
        this.initImageUrl = initImageUrl;

        // 外层布局：横向放置图片容器、上传组件、以及“应用”按钮
        // 这里也可以用 HorizontalLayout，或自己直接设置 style
        FlexLayout layout = new FlexLayout();
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        // 如果不想使用 FlexLayout，也可以继续使用 this.setStyle() 等设置

        // ========= 1. 图片预览容器 =========
        imageContainer = new Div();
        imageContainer.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("width", "150px")
                .set("height", "100px")
                .set("border", "1px solid #ccc")
                .set("border-radius", "8px")
                .set("cursor", "pointer")
                .set("position", "relative")
                .set("overflow", "hidden")
                .set("background-size", "cover")
                .set("background-position", "center");

        if (initImageUrl != null && !initImageUrl.isEmpty()) {
            imageContainer.getStyle().set("background-image", "url('" + initImageUrl + "')");
        } else {
            imageContainer.getStyle()
                    .set("background-color", "#f0f0f0")
                    .set("color", "#555");
        }

        // ========= 2. Upload 组件与上传按钮 =========
        MemoryBuffer buffer = new MemoryBuffer();
        upload = new Upload(buffer);
        // 只接受图片类型
        upload.setAcceptedFileTypes("image/*");
        // 如果只想上传一个文件，可以启用这一行
        // upload.setMaxFiles(1);

        // 自定义上传按钮
        uploadButton = new Button("上传图片");
        // Vaadin 内置主题样式，变成主题色按钮
        uploadButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // 把 Upload 的默认按钮替换为自定义的按钮
        upload.setUploadButton(uploadButton);

        // 不允许拖拽
        upload.setDropAllowed(false);

        // ========= 3. 监听上传成功事件 =========
        upload.addSucceededListener(event -> {
            String fileName = event.getFileName();
            showNotification("Uploaded: " + fileName, NotificationVariant.LUMO_CONTRAST);

            // 将图片转换为 base64
            String newImageUrl = "data:" + event.getMIMEType() + ";base64," + handler.getBase64(buffer);
            // 更新预览容器背景
            imageContainer.getStyle().set("background-image", "url('" + newImageUrl + "')");

            // 调用自定义处理逻辑，获得泛型类型的结果
            uploadResult = handler.handleUploadSucceeded(buffer);
        });

        // ========= 4. 上传完成后，重置上传按钮，并允许再次上传 =========
        upload.addFinishedListener(event -> {
            uploadButton.setText("重新上传");
            uploadButton.setEnabled(true);
            // 清空上传文件列表，以便下一次点击时还能弹出文件对话框
            upload.clearFileList();
        });

        // 如果你使用了 setMaxFiles(1)，可以在这里监听 max-files-reached-changed 事件：
        upload.getElement()
                .addEventListener("max-files-reached-changed", e -> {
                    boolean maxFilesReached = e.getEventData().getBoolean("event.detail.value");
                    uploadButton.setEnabled(!maxFilesReached);
                })
                .addEventData("event.detail.value");

        // ========= 5. “应用”按钮（新增） =========
        applyButton = new Button("应用");
        // 使用 Vaadin 内置的 Success 主题，让按钮看起来是绿色（可根据需要换别的）
        applyButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);

        // 设置一些额外内联样式使其更美观
        applyButton.getStyle()
                // 可以根据需要微调
                .set("margin-left", "5px");

        // 点击“应用”后，将当前上传结果传递给自定义逻辑的 apply 方法
        applyButton.addClickListener(e -> {
            if (uploadResult != null) {
                handler.apply(uploadResult);
                showNotification("应用成功！",NotificationVariant.LUMO_SUCCESS);

            } else {
                showNotification("还没有任何可应用的数据！", NotificationVariant.LUMO_WARNING);
            }
        });

        // ========= 6. 将所有组件添加到布局中，然后把布局加到当前组件里 =========
        // 这里将组件依次添加到 layout
        Div div = new Div(" ");
        div.setWidth("10px");
        layout.add(imageContainer, div, upload, applyButton);
        add(layout);
    }

    /**
     * 可在运行时动态更改初始图片
     */
    public void setInitImageUrl(String initImageUrl) {
        this.initImageUrl = initImageUrl;
        if (initImageUrl != null && !initImageUrl.isEmpty()) {
            imageContainer.getStyle().set("background-image", "url('" + initImageUrl + "')");
        } else {
            imageContainer.getStyle().remove("background-image");
        }
    }

    // ========= 7. 新的泛型接口：处理上传成功，并在“应用”时再执行应用逻辑 =========
    public interface ImageUploadHandler<T> {
        /**
         * 当文件成功上传并转换为 MemoryBuffer 后，执行自定义处理并返回泛型结果
         */
        T handleUploadSucceeded(MemoryBuffer buffer);

        /**
         * 当用户点击“应用”按钮时，使用上一步的结果执行相应逻辑
         */
        void apply(T data);

        /**
         * 默认的 base64 转换，可根据需要修改
         */
        default String getBase64(MemoryBuffer buffer) {
            try (InputStream inputStream = buffer.getInputStream();
                 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                byte[] bufferArray = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(bufferArray)) != -1) {
                    outputStream.write(bufferArray, 0, bytesRead);
                }
                return Base64.getEncoder().encodeToString(outputStream.toByteArray());
            } catch (Exception e) {
                throw new RuntimeException("Failed to convert image to Base64", e);
            }
        }
    }
}
