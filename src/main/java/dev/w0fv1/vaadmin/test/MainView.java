package dev.w0fv1.vaadmin.test;

import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Route;
import dev.w0fv1.vaadmin.view.framework.BaseMainView;

import java.util.List;
@Route("")
public class MainView extends BaseMainView {
    @Override
    protected String getTitle() {
        return "MainViewTitle";
    }

    @Override
    protected List<SideNavItem> getSideNavItems() {
        return List.of(
                new SideNavItem("主页", EchoPage.class, VaadinIcon.HOME.create())
        );
    }
}
