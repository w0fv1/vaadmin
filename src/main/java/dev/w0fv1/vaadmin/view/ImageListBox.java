package dev.w0fv1.vaadmin.view;

import com.vaadin.flow.component.html.Div;
import lombok.Getter;

@Getter
public class ImageListBox<T> extends  ListBox<T> {
    private final T value;

    public ImageListBox(T value, String title) {
        this(value, title, null); // 调用带图片 URL 的构造函数，默认图片为 null
    }

    public ImageListBox(T value, String title, String imageUrl) {
        this.value = value;

        // 设置样式
        getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("width", "150px") // 宽度
                .set("height", "100px") // 高度
                .set("border", "1px solid #ccc")
                .set("border-radius", "8px")
                .set("cursor", "pointer")
                .set("position", "relative")
                .set("overflow", "hidden");

        // 根据是否提供图片设置背景
        if (imageUrl != null && !imageUrl.isEmpty()) {
            getStyle()
                    .set("background-image", "url('" + imageUrl + "')")
                    .set("background-size", "cover")
                    .set("background-position", "center");
        } else {
            // 无图片时设置默认背景颜色
            getStyle()
                    .set("background-color", "#f0f0f0")
                    .set("color", "#555"); // 默认文字颜色
        }

        // 添加标题
        Div titleDiv = new Div();
        titleDiv.setText(title);
        titleDiv.getStyle()
                .set("position", "absolute")
                .set("bottom", "10px")
                .set("left", "10px")
                .set("color", imageUrl != null ? "white" : "#000") // 根据背景调整文字颜色
                .set("background", imageUrl != null ? "rgba(0, 0, 0, 0.5)" : "transparent") // 半透明背景
                .set("padding", "4px 8px")
                .set("border-radius", "4px")
                .set("font-size", "14px")
                .set("overflow", "hidden")
                .set("font-weight", "bold");
        titleDiv.setMaxWidth("114px");

        add(titleDiv);
    }

    @Override
    public String getPreferredHeight() {
        return "100px";
    }

    @Override
    public String getPreferredWidth() {
        return "150px";
    }
}