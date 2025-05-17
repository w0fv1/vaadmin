package dev.w0fv1.vaadmin.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.page.Page;

/**
 * Vaadin 工具类，包含常用操作工具方法。
 * 功能：在新标签页中打开指定 URL。
 */
public class VaadinUtil {

    /**
     * 在浏览器中打开一个新标签页并跳转到指定的 URL。
     *
     * @param url 要打开的目标 URL，必须是合法的 HTTP 或 HTTPS 链接
     */
    public static void openUrlInNewTab(String url) {
        // 获取当前 UI 实例
        UI ui = UI.getCurrent();
        if (ui != null) {
            Page page = ui.getPage();
            // 使用 JavaScript 的 window.open 方法打开新标签页
            page.executeJs("window.open($0, '_blank')", url);
        } else {
            // 若无法获取 UI 实例，则打印错误日志（可替换为 Logger 记录）
            System.err.println("无法获取当前 UI 实例，无法打开新标签页");
        }
    }
}
