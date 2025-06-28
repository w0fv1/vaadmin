package dev.w0fv1.vaadmin.view.tools;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.Timer;
import java.util.TimerTask;

public class LoadingOverlay {

    private static Dialog dialog;

    public static void startLoading(UI ui, String message, Long autoCloseMs) {
        if (dialog != null && dialog.isOpened()) return;

        dialog = new Dialog();
        dialog.setModal(true);
        dialog.setDraggable(false);
        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);

        // 加载圈
        Div spinner = new Div();
        spinner.getElement().getStyle()
                .set("width", "48px")
                .set("height", "48px")
                .set("border", "5px solid #f3f3f3")
                .set("border-top", "5px solid #3498db")
                .set("border-radius", "50%")
                .set("animation", "spin 1s linear infinite");

        // 注入 CSS 动画
        ui.getElement().executeJs("""
            if (!document.getElementById('loading-spinner-style')) {
                const style = document.createElement('style');
                style.id = 'loading-spinner-style';
                style.textContent = `
                  @keyframes spin {
                      0% { transform: rotate(0deg); }
                      100% { transform: rotate(360deg); }
                  }
                `;
                document.head.appendChild(style);
            }
        """);

        // 文本
        Span text = new Span(message != null ? message : "加载中...");
        text.getStyle()
                .set("color", "white")
                .set("font-size", "1.2rem")
                .set("margin-top", "1rem");

        VerticalLayout layout = new VerticalLayout(spinner, text);
        layout.setSizeFull();
        layout.setJustifyContentMode(VerticalLayout.JustifyContentMode.CENTER);
        layout.setAlignItems(VerticalLayout.Alignment.CENTER);
        layout.getStyle().set("background", "transparent");

        dialog.add(layout);
        ui.add(dialog);
        dialog.open();

        // 修改遮罩背景色为半透明（必须使用 JS 修改 Shadow DOM）
        ui.getPage().executeJs("""
            const overlay = document.querySelector('vaadin-dialog-overlay');
            if (overlay) {
                overlay.shadowRoot.querySelector('[part="overlay"]').style.background = "rgba(0,0,0,0.5)";
            }
        """);

        if (autoCloseMs != null && autoCloseMs > 0) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    ui.access(LoadingOverlay::stopLoading);
                }
            }, autoCloseMs);
        }
    }

    public static void stopLoading() {
        if (dialog != null) {
            dialog.close();
            dialog = null;
        }
    }
}
