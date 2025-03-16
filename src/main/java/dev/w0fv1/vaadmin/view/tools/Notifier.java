package dev.w0fv1.vaadmin.view.tools;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class Notifier {
    // 静态工具方法，用于展示不同类型的通知
    public static void showNotification(String message, NotificationVariant variant) {
        Notification notification = new Notification();
        notification.addThemeVariants(variant);
        Div text = new Div(new Text(message));

        Button closeButton = new Button(VaadinIcon.CLOSE.create(), event -> notification.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);

        HorizontalLayout layout = new HorizontalLayout(text, closeButton);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);

        notification.add(layout);
        notification.setPosition(Notification.Position.TOP_CENTER);
        notification.setDuration(2000); // 可选：设置自动关闭时间，单位为毫秒
        notification.open();
    }
    public static void showNotification(String message) {
        showNotification(message, NotificationVariant.LUMO_SUCCESS);
    }
}
