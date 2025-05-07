package dev.w0fv1.vaadmin.view.form.component;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import dev.w0fv1.vaadmin.view.form.model.BaseFormModel;
import lombok.Getter;

import java.lang.reflect.Field;

/**
 * BaseDialogFormFieldComponent
 * 基于弹窗（Dialog）的表单字段基类，绑定任意类型数据。
 * <p>
 * 设计要求：
 * - 只在initStaticView初始化控件；
 * - 组件自己持有数据，不依赖UI控件；
 * - 数据与界面刷新完全分离。
 *
 * @param <Type> 绑定的数据类型
 */
@Getter
public abstract class BaseDialogFormFieldComponent<Type> extends BaseFormFieldComponent<Type> {

    protected Dialog dialog;           // 弹窗
    protected Button openDialogButton; // 打开弹窗按钮
    private Type data;                  // 持有的数据

    public BaseDialogFormFieldComponent(Field field, BaseFormModel formModel) {
        super(field, formModel);
    }


    @Override
    void initStaticView() {
        this.dialog = new Dialog();
        this.dialog.setCloseOnEsc(true);
        this.dialog.setCloseOnOutsideClick(true);
        this.dialog.add(createStaticDialogContent());

        openDialogButton = new Button("打开对话框", event -> {
            if (dialog != null) {
                dialog.open();
            }
        });
        add(openDialogButton);
    }


    @Override
    public void pushViewData() {
        if (openDialogButton != null) {
            openDialogButton.setText( getButtonText() );
        }
    }

    @Override
    public Type getData() {
        return data;
    }

    @Override
    public void setData(Type data) {
        this.data = data;
    }

    @Override
    public void clearData() {
        this.data = null;
    }

    @Override
    public void clearUI() {
        // 弹窗本身一般不需要清除
        if (openDialogButton != null) {
            openDialogButton.setText("新建 " + getFormField().title());
        }
    }

    public String getButtonText() {
        if (data != null) {
            return "编辑 " + getFormField().title();
        }
        return "新建 " + getFormField().title();
    }

    /**
     * 子类必须实现，构建对话框内部内容。
     *
     * @return 对话框内部的 VerticalLayout 布局
     */
    protected abstract VerticalLayout createStaticDialogContent();

}
