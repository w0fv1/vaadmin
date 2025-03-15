package dev.w0fv1.vaadmin.test;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.Route;
import dev.w0fv1.vaadmin.view.*;
import dev.w0fv1.vaadmin.view.form.NormalForm;
import dev.w0fv1.vaadmin.view.form.RepositoryForm;
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
        ListBoxGroup<String> horizontalListBoxGroup = new ListBoxGroup<>(ListBoxGroup.Orientation.HORIZONTAL);
        horizontalLayout.add(horizontalListBoxGroup);
        // 添加 ListBox（无背景图片）
        horizontalListBoxGroup.addListBox(new ImageListBox<>("item1", "Default Background"));

        // 添加 ListBox（带背景图片）
        horizontalListBoxGroup.addListBox(new ImageListBox<>(
                "item2",
                "Image Background",
                "https://via.placeholder.com/150" // 示例图片 URL
        ));
        // 设置添加按钮的行为
        horizontalListBoxGroup.setOnAddButtonClick(() -> {
            // 点击 "+" 按钮时添加一个新项（无背景图片）
            horizontalListBoxGroup.addListBox(new ImageListBox<>("newItem", "New Default Item"));
        });
        // 设置顺序变更回调
        horizontalListBoxGroup.setOnOrderChangeListener(newOrder -> {
            System.out.println("Order changed: " + Arrays.toString(newOrder.toArray()));
        });

        ListBoxGroup<String> verticallListBoxGroup = new ListBoxGroup<>(ListBoxGroup.Orientation.VERTICAL);

        verticallListBoxGroup.addListBox(new TextListBox<>("item3", "Default Background"));
        verticallListBoxGroup.addListBox(new TextListBox<>("item3", "Default Background"));
        verticallListBoxGroup.addListBox(new TextListBox<>("item3", "Default Background"));
        verticallListBoxGroup.addListBox(new TextListBox<>("item3", "Default Background"));
        verticallListBoxGroup.addListBox(new TextListBox<>("item3", "Default Background"));
        verticallListBoxGroup.addListBox(new TextListBox<>("item3", "Default Background"));
        verticallListBoxGroup.addListBox(new TextListBox<>("item3", "Default BackgroundDefault BackgroundDefault BackgroundDefault Background"));
        horizontalLayout.add(verticallListBoxGroup);

        // 设置添加按钮的行为
        verticallListBoxGroup.setOnAddButtonClick(() -> {
            // 点击 "+" 按钮时添加一个新项（无背景图片）
            verticallListBoxGroup.addListBox(new TextListBox<>("newItem", "New Default Item"));
        });


        ImageUploadButton<String> imageUploadButton = new ImageUploadButton<String>(null, new ImageUploadButton.ImageUploadHandler<String>() {
            @Override
            public String handleUploadSucceeded(MemoryBuffer buffer) {
                return "success";
            }

            @Override
            public void apply(String data) {
                log.info("upload " + data);
            }
        });
        horizontalLayout.add(imageUploadButton);

        TextInput textInput = new TextInput("测试", new SerializableConsumer<String>() {
            @Override
            public void accept(String s) {
                log.info("textInput:{}", s);
            }
        });
        horizontalLayout.add(textInput);


        // 1. 创建一个按钮
        Button openFormButton = new Button("点击弹出表单");

        // 2. 给按钮添加点击事件
        openFormButton.addClickListener(event -> {
            // 2.1 创建表单数据对象
            EchoF echoF = new EchoF();
            Dialog dialog = new Dialog();

            // 2.2 构造一个 NormalForm
            NormalForm<EchoF> normalForm = new NormalForm<>(
                    echoF,
                    savedData -> {
                        // onSave 回调逻辑
                        Notification.show("保存成功，用户输入：" + savedData.toString());
                    },
                    () -> {
                        dialog.close();
                        // onCancel 回调逻辑
                        Notification.show("用户取消了操作");
                    }
            );

            // 2.3 创建一个 Dialog，并将表单添加进去
            dialog.add(normalForm);

            // 可以根据需要设置 dialog 尺寸
            dialog.setWidth("600px");
            dialog.setHeight("80vh");

            // 打开对话框
            dialog.open();
        });

        // 3. 将按钮添加到布局
        horizontalLayout.add(openFormButton);


        Button button = new Button("客制化创建");

        button.addClickListener((ComponentEventListener<ClickEvent<Button>>) event -> {
            Dialog customCreateDialog = new Dialog();
            VerticalLayout dialogLayout = null;
            dialogLayout = new RepositoryForm<>(
                    new EchoF("预制内容预制内容预制内容预制内容预制内容"),
                    (Long id) -> {
                        customCreateDialog.close();
                        reloadCurrentData();
                    }, () -> {
                customCreateDialog.close();
                reloadCurrentData();
            }, genericRepository
            );
            customCreateDialog.add(dialogLayout);
            add(customCreateDialog);
            customCreateDialog.open();
        });

        horizontalLayout.add(button);


        TabSection.TabItem<String> tab1 = new TabSection.TabItem<>("Tab 1", "tab1", new Span("这是Tab 1的内容"));
        TabSection.TabItem<String> tab2 = new TabSection.TabItem<>("Tab 2", "tab2", new Span("这是Tab 2的内容"));
        TabSection.TabItem<String> tab3 = new TabSection.TabItem<>("Tab 3", "tab3", new Span("这是Tab 3的内容"));

        TabSection<String> tabSection = new TabSection<>(Arrays.asList(tab1, tab2, tab3));

        tabSection.addTab(new TabSection.TabItem<>("设置", "settings", new Span("设置内容")));
        tabSection.onTabSelected(value -> {
            Notification.show("选中Tab值：" + value);
        });
        // 示例：获取当前选中的Tab value

        horizontalLayout.add(tabSection);

        return horizontalLayout;
    }
}
