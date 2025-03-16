package dev.w0fv1.vaadmin.view.framework;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.util.List;

public abstract class BaseMainView extends AppLayout {

    // 保存 SideNav 引用
    protected SideNav sideNav;

    public BaseMainView() {
        DrawerToggle toggle = new DrawerToggle();
        H5 title = new H5(getTitle());
        this.sideNav = createSideNav(); // 使用更新后的 createSideNav()
        Scroller scroller = new Scroller(sideNav);
        scroller.setClassName(LumoUtility.Padding.SMALL);
        addToDrawer(scroller);
        addToNavbar(toggle, title);
        setPrimarySection(Section.DRAWER);
    }

    private SideNav createSideNav() {
        SideNav nav = new SideNav();
        // 初始化时加载导航项
        for (SideNavItem item : getSideNavItems()) {
            nav.addItem(item);
        }
        return nav;
    }

    /**
     * 动态刷新侧边导航栏，重新加载导航项
     */
    protected void updateSideNav() {
        sideNav.removeAll();
        List<SideNavItem> items = getSideNavItems();
        for (SideNavItem item : items) {
            sideNav.addItem(item);
        }
    }

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

    protected abstract String getTitle();

    /**
     * 子类需要提供侧边导航项列表，后续可以根据需要动态更新这些项
     */
    protected abstract List<SideNavItem> getSideNavItems();
}
