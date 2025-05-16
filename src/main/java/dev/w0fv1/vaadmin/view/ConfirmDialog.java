package dev.w0fv1.vaadmin.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class ConfirmDialog extends Dialog {

    public ConfirmDialog(String action, Runnable onConfirm) {
        // 弹窗标题
        H3 title = new H3("您确认要执行 “" + action + "” 吗？");

        // 确定按钮
        Button confirmButton = new Button("确定", event -> {
            onConfirm.run();  // 执行传入的行为
            this.close();     // 关闭弹窗
        });

        // 取消按钮
        Button cancelButton = new Button("取消", event -> this.close());

        // 布局
        HorizontalLayout buttons = new HorizontalLayout(confirmButton, cancelButton);
        buttons.setSpacing(true);

        VerticalLayout content = new VerticalLayout(title, buttons);
        content.setSpacing(true);
        content.setPadding(true);

        add(content);
        setModal(true); // 禁止点击遮罩关闭
        setCloseOnEsc(true); // 支持 ESC 键退出
        setCloseOnOutsideClick(false); // 禁止点击外部关闭
    }
}
