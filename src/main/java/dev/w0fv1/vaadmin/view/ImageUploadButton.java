package dev.w0fv1.vaadmin.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Base64;

public class ImageUploadButton extends Div {

    private String initImageUrl;
    private final Div imageContainer;
    private final Button uploadButton;
    private final Upload upload;

    public ImageUploadButton(String initImageUrl, ImageUploadHandler handler) {
        this.initImageUrl = initImageUrl;

        // Layout container
        this.getStyle().set("display", "flex")
                .set("align-items", "center")
                .set("gap", "10px");

        // Image container
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

        // Upload functionality
        MemoryBuffer buffer = new MemoryBuffer();
        upload = new Upload(buffer);
        upload.setAcceptedFileTypes("image/*");

        upload.addSucceededListener(event -> {
            String fileName = event.getFileName();
            Notification.show("Uploaded: " + fileName, 2000, Notification.Position.MIDDLE);

            // Update the background image
            String newImageUrl = "data:" + event.getMIMEType() + ";base64," + handler.getBase64(buffer);
            imageContainer.getStyle().set("background-image", "url('" + newImageUrl + "')");

            // Call the handler
            handler.handleUploadSucceeded(buffer);
        });

        upload.setDropAllowed(false); // Disable drop area

        // Custom upload button
        uploadButton = new Button("Upload Image");
        uploadButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        upload.setUploadButton(uploadButton);

        // Trigger file selection when the button is clicked
        uploadButton.addClickListener(event -> upload.getElement().callJsFunction("click"));

        // Disable the button when max files are reached
        upload.getElement()
                .addEventListener("max-files-reached-changed", event -> {
                    boolean maxFilesReached = event.getEventData()
                            .getBoolean("event.detail.value");
                    uploadButton.setEnabled(!maxFilesReached);
                }).addEventData("event.detail.value");

        // Add components to the layout
        add(imageContainer, upload);
    }

    public void setInitImageUrl(String initImageUrl) {
        this.initImageUrl = initImageUrl;
        if (initImageUrl != null && !initImageUrl.isEmpty()) {
            imageContainer.getStyle().set("background-image", "url('" + initImageUrl + "')");
        } else {
            imageContainer.getStyle().remove("background-image");
        }
    }

    public interface ImageUploadHandler {
        void handleUploadSucceeded(MemoryBuffer buffer);

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
