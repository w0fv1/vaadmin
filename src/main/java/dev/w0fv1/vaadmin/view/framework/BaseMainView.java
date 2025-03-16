package dev.w0fv1.vaadmin.view.framework;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.util.List;

public abstract class BaseMainView extends AppLayout {

    protected SideNav sideNav;
    private H5 title;


    /**
     * 初始化UI组件
     */
    protected void initView() {
        DrawerToggle toggle = new DrawerToggle();
        title = new H5();
        sideNav = new SideNav();

        Scroller scroller = new Scroller(sideNav);
        scroller.setClassName(LumoUtility.Padding.SMALL);

        addToDrawer(scroller);
        addToNavbar(toggle, title);
        setPrimarySection(Section.DRAWER);
    }


    protected void setTitleText(String titleText) {
        title.setText(titleText);
    }

    /**
     * 动态刷新侧边导航栏
     */
    protected void setSideNavItems(List<SideNavItem> sideNavItems) {
        sideNav.removeAll();
        if (sideNavItems != null) {
            for (SideNavItem item : sideNavItems) {
                sideNav.addItem(item);
            }
        }
    }

}