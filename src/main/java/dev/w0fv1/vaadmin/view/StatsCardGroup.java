package dev.w0fv1.vaadmin.view;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * 统计卡片组：将若干 {@link StatsCard} 分类显示。
 */
public class StatsCardGroup extends Composite<VerticalLayout> {

    private final FlexLayout cardContainer = new FlexLayout();

    /**
     * @param title 组标题
     */
    public StatsCardGroup(String title) {
        H4 header = new H4(Objects.requireNonNull(title, "标题不能为空"));
        header.addClassNames(LumoUtility.Margin.NONE,
                LumoUtility.FontSize.XLARGE,
                LumoUtility.FontWeight.BOLD);

        cardContainer.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        cardContainer.getStyle().set("gap", "var(--lumo-space-m)");

        VerticalLayout root = getContent();
        root.add(header, cardContainer);
        root.addClassNames(LumoUtility.Padding.LARGE,
                LumoUtility.BorderRadius.LARGE,
                LumoUtility.Border.ALL,
                LumoUtility.BoxShadow.SMALL);
    }

    /**
     * 直接添加已实例化的卡片。
     */
    public void addCard(StatsCard card) {
        cardContainer.add(card);
    }

    /**
     * 方便工厂：一行代码搞定“创建 + 添加”。
     */
    public StatsCard createAndAddCard(Icon icon,
                                      String title,
                                      Supplier<String> supplier,
                                      Duration refresh) {
        StatsCard card = new StatsCard(icon, title, supplier, refresh);
        addCard(card);
        return card;
    }
}
