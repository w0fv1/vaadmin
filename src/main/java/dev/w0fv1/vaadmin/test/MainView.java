package dev.w0fv1.vaadmin.test;

import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.AttachEvent;
import dev.w0fv1.vaadmin.view.framework.BaseMainView;
import dev.w0fv1.vaadmin.view.tools.UITimer;

import java.util.List;

@Route("")
public class MainView extends BaseMainView {


    public MainView() {
        initView();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        setTitleText("MainViewTitle");
        setSideNavItems(List.of(new SideNavItem("主页", "/home", VaadinIcon.HOME.create())));

    }


}
