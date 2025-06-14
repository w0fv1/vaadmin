package dev.w0fv1.vaadmin.test;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.Route;
import dev.w0fv1.vaadmin.view.*;
import dev.w0fv1.vaadmin.view.form.NormalForm;
import dev.w0fv1.vaadmin.view.form.RepositoryForm;
import dev.w0fv1.vaadmin.view.table.BaseRepositoryTablePage;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.text.NumberFormat;
import java.util.Locale;
import java.time.Duration;

// 若需要直接使用 StatsCard 类型，可再 import com.example.stats.StatsCard;

import static dev.w0fv1.vaadmin.view.tools.Notifier.showNotification;

/**
 * Echo 管理页（适配新版生命周期：静态 UI → 数据 → Push）。
 */
@Slf4j
@Route(value = "/home", layout = MainView.class)
public class EchoRepositoryPage extends BaseRepositoryTablePage<EchoT, EchoF, Echo, Long> {

    private final EchoService echoService;

    public EchoRepositoryPage(EchoService echoService) {
        // 传入默认表单模型，方便 "创建" 弹窗预置内容
        super(EchoT.class, EchoF.class, new EchoF("TEST EchoF 默认内容"), Echo.class);
        this.echoService = echoService;
        initialize();
    }


    /* -------------------------------------------------- 查询谓词 -------------------------------------------------- */

    /** 默认仅显示 NORMAL 状态 */
    @Override
    public void presetPredicate() {
//        predicateManager.putPredicate("statusIsNormal", (cb, root, predicates) ->
//                predicates.add(cb.equal(root.get("status"), Echo.Status.NORMAL))
//        );
    }

    /* -------------------------------------------------- Grid 扩展列 ------------------------------------------------ */
    @Override
    public void extendGridColumns() {
        // 示例：简单列 —— 按钮展示 message
        extendGridComponentColumn(echoT -> new Button(echoT.getMessage()))
                .setHeader("TEST");

        // 示例：更新列 —— 打开 Dialog 编辑 message
        extendGridComponentColumn(echoT -> {
            Button editBtn = new Button("修改信息", VaadinIcon.EDIT.create());
            editBtn.addClickListener(e -> openEditMessageDialog(echoT));
            return editBtn;
        }).setHeader("功能").setAutoWidth(true);
    }

    private void openEditMessageDialog(EchoT echoT) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("修改 Message");
        dialog.add("这个操作将修改回声的 Message");

        TextField messageField = new TextField("新 Message");
        messageField.setWidthFull();
        dialog.add(messageField);

        Button cancel = new Button("取消", e -> dialog.close());
        Button save = new Button("保存", e -> {
            String newMsg = messageField.getValue();
            echoService.updateMessage(echoT.getId(), newMsg);
            refresh();
            dialog.close();
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getFooter().add(cancel, save);
        dialog.open();
    }

    /* -------------------------------------------------- Action 扩展区 ------------------------------------------------ */

    /** 子操作：打开实体选择器 */
    @Override
    public Component extendSecondaryAction() {
        return new Button("打开数据选择测试", e -> {
            Dialog dialog = new Dialog();
            EntitySelectPage<Echo, Long> selectPage = new EntitySelectPage<>(
                    Echo.class,
                    selected -> {
                        log.info("selected: {}", selected);
                        dialog.close();
                    },
                    true,
                    genericRepository,
                    (cb, root, predicates) -> predicates.add(cb.equal(root.get("status"), Echo.Status.NORMAL))
            );
            dialog.add(selectPage);
            add(dialog);
            dialog.open();
        });
    }

    /** 数据栏额外按钮 */
    @Override
    public Component extendDataAction() {
        return new Button("extDataAction Message");
    }

    /** 标题栏右侧主操作 */
    @Override
    public Component extendPrimaryAction() {
        Button randomBtn = new Button("随机创建", e -> {
            echoService.randomEcho();
            refresh();
        });
        return new HorizontalLayout(randomBtn);
    }

    /* -------------------------------------------------- 持久化回调 ------------------------------------------------- */
    @Override
    public void onSave(Long id) {
        showNotification(id + " 被创建了", NotificationVariant.LUMO_SUCCESS);
    }

    /* -------------------------------------------------- 页面底部扩展 ------------------------------------------------ */
    @Override
    public Component extendPage() {
        VerticalLayout layout = new VerticalLayout();

        /* ---------- ListBoxGroup 示例 ---------- */
        ListBoxGroup<String> horizGroup = new ListBoxGroup<>(ListBoxGroup.Orientation.HORIZONTAL);
        horizGroup.addListBox(new ImageListBox<>("item1", "Default Background"));
        horizGroup.addListBox(new ImageListBox<>("item2", "Image Background", "https://via.placeholder.com/150"));
        horizGroup.setOnAddButtonClick(() -> horizGroup.addListBox(new ImageListBox<>("new", "New Item")));
        horizGroup.setOnOrderChangeListener(order -> log.info("Order changed: {}", Arrays.toString(order.toArray())));
        layout.add(horizGroup);

        ListBoxGroup<String> vertGroup = new ListBoxGroup<>(ListBoxGroup.Orientation.VERTICAL);
        vertGroup.setOnAddButtonClick(() -> vertGroup.addListBox(new TextListBox<>("new", "New Item")));
        layout.add(vertGroup);

        /* ---------- 图片上传按钮 ---------- */
        ImageUploadButton<String> uploadBtn = new ImageUploadButton<>(null, new ImageUploadButton.ImageUploadHandler<>() {
            @Override public String handleUploadSucceeded(MemoryBuffer buffer) { return "success"; }
            @Override public void apply(String data) { log.info("upload: {}", data); }
        });
        layout.add(uploadBtn);

        /* ---------- TextInput ---------- */
        TextInput textInput = new TextInput("测试", value -> log.info("textInput: {}", value));
        layout.add(textInput);

        /* ---------- 按钮弹窗表单 ---------- */
        Button openForm = new Button("点击弹出表单", e -> openNormalFormDialog());
        layout.add(openForm);

        /* ---------- 自定义创建（RepositoryForm） ---------- */
        Button customCreate = new Button("客制化创建", e -> openCustomCreateDialog());
        layout.add(customCreate);

        /* ---------- TabSection ---------- */
        TabSection<String> tabSection = new TabSection<>(Arrays.asList(
                new TabSection.TabItem<>("Tab 1", "tab1", new Span("Tab1 内容")),
                new TabSection.TabItem<>("Tab 2", "tab2", new Span("Tab2 内容")),
                new TabSection.TabItem<>("Tab 3", "tab3", new Span("Tab3 内容"))
        ));
        tabSection.addTab(new TabSection.TabItem<>("设置", "settings", new Span("设置内容")));
        tabSection.onTabSelected(val -> showNotification("选中 Tab 值：" + val));
        layout.add(tabSection);

        /* ---------- 示例数据统计卡片 ---------- */
        StatsCardGroup statsGroup = new StatsCardGroup("示例数据统计");
        statsGroup.createAndAddCard(
                VaadinIcon.MONEY.create(),
                "昨日收入",
                () -> {
                    double amount = 50000 + Math.random() * 100000; // 5万 ~ 15万

                    return NumberFormat.getCurrencyInstance(Locale.CHINA).format(amount );
                },
                Duration.ofSeconds(3));

        statsGroup.createAndAddCard(
                VaadinIcon.CREDIT_CARD.create(),
                "本月累计收入",
                () -> NumberFormat.getCurrencyInstance(Locale.CHINA).format(9876543.21),
                Duration.ofMinutes(2));

        statsGroup.createAndAddCard(
                VaadinIcon.GROUP.create(),
                "昨日活跃用户",
                () -> NumberFormat.getIntegerInstance(Locale.CHINA).format(45231),
                Duration.ofSeconds(45));

        layout.add(statsGroup);
        /* ---------- 统计卡片示例结束 ---------- */

        return layout;
    }

    /* -------------------------------------------------- 工具方法 -------------------------------------------------- */
    private void openNormalFormDialog() {
        Dialog dialog = new Dialog();
        EchoF formModel = new EchoF();
        NormalForm<EchoF> normalForm = new NormalForm<>(
                formModel,
                saved -> showNotification("保存成功：" + saved, NotificationVariant.LUMO_SUCCESS),
                () -> { dialog.close(); showNotification("用户取消", NotificationVariant.LUMO_WARNING);} );
        dialog.add(normalForm);
        dialog.setWidth("600px");
        dialog.setHeight("80vh");
        dialog.open();
    }

    private void openCustomCreateDialog() {
        Dialog dlg = new Dialog();
        RepositoryForm<EchoF, Echo, Long> repoForm = new RepositoryForm<>(
                new EchoF("预制内容"),
                id -> { dlg.close(); refresh(); },
                () -> { dlg.close(); refresh(); },
                genericRepository
        );
        repoForm.initialize();
        dlg.add(repoForm);
        dlg.open();
    }

    /* -------------------------------------------------- 其它覆写 -------------------------------------------------- */
    @Override public Boolean enableCreate() { return true; }
    @Override public Boolean enableUpdate() {
        return super.enableUpdate();
    }

    @Override
    public void onGetUrlQueryParameters(ParameterMap parameters, BeforeEnterEvent event) {
        log.info("onGetUrlParameters: {}", parameters);
    }
}
