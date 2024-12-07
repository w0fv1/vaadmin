package dev.w0fv1.vaadmin.test;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.Route;
import dev.w0fv1.vaadmin.view.EntitySelectPage;
import dev.w0fv1.vaadmin.view.ListBoxGroup;
import dev.w0fv1.vaadmin.view.table.RepositoryBaseTableManagementPage;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

import static dev.w0fv1.vaadmin.view.framework.BaseMainView.showNotification;

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
        return new Button("打开数据选择测试", new ComponentEventListener<ClickEvent<Button>>() {
            @Override
            public void onComponentEvent(ClickEvent<Button> buttonClickEvent) {
                Dialog dialog = new Dialog();
                EntitySelectPage<Echo, Long> selectPage = new EntitySelectPage<>(
                        Echo.class,
                        (d) -> {
                            log.info("select :{}", d);
                            dialog.close();
                        },
                        true, // Set to true for single selection
                        genericRepository,
                        (cb, root, predicates) -> predicates.add(cb.equal(root.get("status"), Echo.Status.NORMAL))
                );


                dialog.add(selectPage);

                add(dialog);
                dialog.open();
            }
        });
    }

    @Override
    public Component extDataAction() {
        return new Button("extDataAction Message");
    }

    @Override
    public Component extTitleAction() {
        HorizontalLayout horizontalLayout = new HorizontalLayout();

        Button button = new Button("随机创建");

        button.addClickListener(new ComponentEventListener<>() {
            @Override
            public void onComponentEvent(ClickEvent<Button> buttonClickEvent) {
                echoService.randomEcho();
                EchoPage.super.refresh();
            }
        });

        horizontalLayout.add(button);

        return horizontalLayout;
    }

    @Override
    public void onSave(Long aLong) {
        showNotification(aLong + "被创建了", NotificationVariant.LUMO_SUCCESS);
    }

    @Override
    public Component extPage() {
        VerticalLayout horizontalLayout = new VerticalLayout();
        // 创建 ListBoxGroup 实例
        ListBoxGroup<String> listBoxGroup = new ListBoxGroup<>();

        // 添加 ListBox（无背景图片）
        listBoxGroup.addListBox(new ListBoxGroup.ListBox<>("item1", "Default Background"));

        // 添加 ListBox（带背景图片）
        listBoxGroup.addListBox(new ListBoxGroup.ListBox<>(
                "item2",
                "Image Background",
                "https://via.placeholder.com/150" // 示例图片 URL
        ));

        // 添加 ListBox（带背景图片）
        listBoxGroup.addListBox(new ListBoxGroup.ListBox<>(
                "item2",
                "Image Background",
                "https://via.placeholder.com/150" // 示例图片 URL
        ));

        // 添加 ListBox（带背景图片）
        listBoxGroup.addListBox(new ListBoxGroup.ListBox<>(
                "item2",
                "Image Background",
                "https://via.placeholder.com/150" // 示例图片 URL
        ));

        // 添加 ListBox（带背景图片）
        listBoxGroup.addListBox(new ListBoxGroup.ListBox<>(
                "item2",
                "Image Background + https://via.placeholder.com/150",
                "https://via.placeholder.com/150" // 示例图片 URL
        ));

        // 添加 ListBox（带背景图片）
        listBoxGroup.addListBox(new ListBoxGroup.ListBox<>(
                "item2",
                "Image Background",
                "https://via.placeholder.com/150" // 示例图片 URL
        ));

        // 添加 ListBox（带背景图片）
        listBoxGroup.addListBox(new ListBoxGroup.ListBox<>(
                "item2",
                "Image Background",
                "https://via.placeholder.com/150" // 示例图片 URL
        ));

        // 设置添加按钮的行为
        listBoxGroup.setOnAddButtonClick(() -> {
            // 点击 "+" 按钮时添加一个新项（无背景图片）
            listBoxGroup.addListBox(new ListBoxGroup.ListBox<>("newItem", "New Default Item"));
        });

        // 设置顺序变更回调
        listBoxGroup.setOnOrderChangeListener(newOrder -> {
            System.out.println("Order changed: " + Arrays.toString(newOrder.toArray()));
        });

        horizontalLayout.add(listBoxGroup);





        return horizontalLayout ;
    }
}
