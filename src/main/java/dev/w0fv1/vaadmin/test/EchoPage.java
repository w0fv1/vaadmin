package dev.w0fv1.vaadmin.test;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.Route;
import dev.w0fv1.vaadmin.view.table.RepositoryBaseTableManagementPage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Route(value = "/home", layout = MainView.class)
public class EchoPage extends RepositoryBaseTableManagementPage<EchoT, EchoF, Echo, Long> {
    private final EchoService echoService;

    public EchoPage(EchoService echoService) {
        super(EchoT.class, EchoF.class, Echo.class);
        this.echoService = echoService;
    }

    @Override
    public void extColumns() {
        extComponentColumn((ValueProvider<EchoT, Component>) echoT -> new Button(echoT.getMessage())).setHeader("TEST");

        extComponentColumn((ValueProvider<EchoT, Component>) echoT -> new Button(echoT.getMessage())).setHeader("Update Message");

        extComponentColumn((ValueProvider<EchoT, Component>) echoT -> {
            HorizontalLayout horizontalLayout = new HorizontalLayout();

            Button button = new Button("修改信息", new ComponentEventListener<ClickEvent<Button>>() {
                @Override
                public void onComponentEvent(ClickEvent<Button> event) {
                    Dialog dialog = new Dialog();

                    dialog.setHeaderTitle("修改Message");
                    dialog.add("这个操作将修改回声的Message");
                    VerticalLayout dialogLayout = new VerticalLayout();
                    TextField titleField = new TextField("新Message");
                    dialogLayout.setPadding(false);
                    dialogLayout.add(titleField);
                    dialog.add(dialogLayout);
                    dialog.setModal(false);

                    Button cancelButton = new Button("取消", e -> dialog.close());
                    Button saveButton = new Button("保存", e -> {
                        String newMessage = titleField.getValue();
                        log.info("newMessage " + newMessage);
                        echoService.updateMessage(echoT.getId(), newMessage);
                        refresh();
                    });
                    saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

                    dialog.getFooter().add(cancelButton);
                    dialog.getFooter().add(saveButton);
                    dialog.open();
                }
            });

            horizontalLayout.add(button);
            return horizontalLayout;
        }).setHeader("功能").setAutoWidth(true);
    }


    @Override
    public Component extSubAction() {
        return new Button("SubAction Message");
    }

    @Override
    public Component extDataAction() {
        return new Button("extDataAction Message");
    }
}
