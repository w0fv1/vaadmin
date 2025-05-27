package dev.w0fv1.vaadmin.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class ConfirmDialog extends Dialog {

    /**
     * 构造确认弹窗（带操作名和确认回调）
     * @param action 操作名称（用于生成标题）
     * @param onConfirm 确认后执行的逻辑
     */
    public ConfirmDialog(String action, Runnable onConfirm) {
        this(action, "", onConfirm);
    }

    /**
     * 构造确认弹窗（带操作名、描述和确认回调）
     * @param action 操作名称（用于生成标题）
     * @param description 描述说明，可为空
     * @param onConfirm 确认后执行的逻辑
     */
    public ConfirmDialog(String action, String description, Runnable onConfirm) {
        H3 title = new H3("您确认要执行 “" + action + "” 吗？");
        Paragraph desc = new Paragraph(description);
        desc.getStyle().set("font-size", "var(--lumo-font-size-s)");
        desc.getStyle().set("color", "var(--lumo-secondary-text-color)");

        Button confirmButton = new Button("确定", (event) -> {
            onConfirm.run();
            this.close();
        });
        Button cancelButton = new Button("取消", (event) -> this.close());

        HorizontalLayout buttons = new HorizontalLayout(confirmButton, cancelButton);
        buttons.setSpacing(true);

        VerticalLayout content = new VerticalLayout();
        content.add(title);
        if (description != null && !description.isBlank()) {
            content.add(desc);
        }
        content.add(buttons);

        content.setSpacing(true);
        content.setPadding(true);

        this.add(content);
        this.setModal(true);
        this.setCloseOnEsc(true);
        this.setCloseOnOutsideClick(false);
    }
}
