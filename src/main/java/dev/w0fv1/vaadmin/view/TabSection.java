package dev.w0fv1.vaadmin.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import lombok.Data;
import lombok.Getter;

import java.util.*;
import java.util.function.Consumer;

public class TabSection<T> extends VerticalLayout {

    private Tabs tabs;
    private Div contentArea;
    private Map<Tab, Component> tabToComponent;
    private Map<Tab, T> tabToValue;
    private List<TabItem<T>> items;
    @Getter
    private T selectedValue;
    private List<Consumer<T>> selectionListeners = new ArrayList<>();

    public TabSection(List<TabItem<T>> items) {
        this.items = new ArrayList<>(items);
        tabToComponent = new LinkedHashMap<>();
        tabToValue = new LinkedHashMap<>();

        tabs = new Tabs();
        tabs.setWidthFull();
        contentArea = new Div();
        contentArea.setWidthFull();

        for (TabItem<T> item : items) {
            addTabInternal(item);
        }

        tabs.addSelectedChangeListener(event -> {
            selectTab(event.getSelectedTab());
            notifySelectionListeners();
        });

        if (!items.isEmpty()) {
            tabs.setSelectedIndex(0);
            selectTab(tabs.getSelectedTab());
        }

        add(tabs, contentArea);
    }

    private void selectTab(Tab selectedTab) {
        Component selectedComponent = tabToComponent.get(selectedTab);
        selectedValue = tabToValue.get(selectedTab);
        contentArea.removeAll();
        contentArea.add(selectedComponent);
    }

    private void addTabInternal(TabItem<T> item) {
        Tab tab = new Tab(item.getLabel());
        tabToComponent.put(tab, item.getComponent());
        tabToValue.put(tab, item.getValue());
        tabs.add(tab);
    }

    public void addTab(TabItem<T> item) {
        items.add(item);
        addTabInternal(item);

        if (tabs.getComponentCount() == 1) {
            tabs.setSelectedIndex(0);
            selectTab(tabs.getSelectedTab());
            notifySelectionListeners();
        }
    }

    public void setSelectedValue(T value) {
        for (Map.Entry<Tab, T> entry : tabToValue.entrySet()) {
            if (Objects.equals(entry.getValue(), value)) {
                tabs.setSelectedTab(entry.getKey());
                return;
            }
        }
    }

    /**
     * 对外统一暴露Tab选择事件
     */
    public void onTabSelected(Consumer<T> listener) {
        selectionListeners.add(listener);
    }

    private void notifySelectionListeners() {
        selectionListeners.forEach(listener -> listener.accept(selectedValue));
    }

    @Data
    public static class TabItem<T> {
        private String label;
        private T value;
        private Component component;

        public TabItem(String label, T value, Component component) {
            this.label = label;
            this.value = value;
            this.component = component;
        }

    }

}

