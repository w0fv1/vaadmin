package dev.w0fv1.vaadmin.view.form.component;

import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import dev.w0fv1.vaadmin.util.TypeUtil;
import dev.w0fv1.vaadmin.view.ErrorMessage;
import dev.w0fv1.vaadmin.view.form.model.FormField;
import dev.w0fv1.vaadmin.view.form.model.BaseFormModel;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;

import static dev.w0fv1.vaadmin.component.FieldValidator.validField;
import static dev.w0fv1.vaadmin.util.TypeUtil.defaultIfNull;

/**
 * BaseFormFieldComponent
 * 所有表单字段组件的抽象父类，封装了字段绑定、数据管理、校验等通用逻辑。
 * 设计要求：
 * 1. 组件必须持有自己的数据，不依赖UI组件暂存；
 * 2. initStaticView()：初始化UI控件，只涉及静态结构，不处理数据；
 * 3. initData()：初始化组件数据，可以在子类中重写，但必须调用super.initData()；
 * 4. pushViewData()：根据当前数据刷新UI，要求幂等（相同数据多次调用不会导致UI异常）；
 * 5. getData()/setData()：只处理数据，不操作任何UI控件；
 * 6. 支持自动初始化数据，支持清空和校验。
 *
 * @param <Type> 当前字段绑定的数据类型（如 String, List<String>）
 */
@Slf4j
@Getter
public abstract class BaseFormFieldComponent<Type> extends VerticalLayout {
    private final Field field; // 反射字段
    private final BaseFormModel formModel; // 表单数据模型
    private final FormField formField; // 字段注解
    private ErrorMessage errorMessage; // 错误提示信息
    private final Boolean autoInitialize; // 是否自动初始化数据

    public BaseFormFieldComponent(Field field, BaseFormModel formModel) {
        this(field, formModel, true);
    }

    public BaseFormFieldComponent(Field field, BaseFormModel formModel, Boolean autoInitialize) {
        this.field = field;
        this.formModel = formModel;
        this.formField = field.getAnnotation(FormField.class);
        this.autoInitialize = autoInitialize;
        this.setPadding(false);

        buildTitle();
    }
    @PostConstruct
    public void initialize() {
        initStaticView();
        initData();
        pushViewData();
    }

    /**
     * 初始化静态UI控件，子类必须实现。
     * 只负责结构搭建，不处理数据。
     */
    abstract void initStaticView();

    /**
     * 构建表单标题、描述信息。
     */
    public void buildTitle() {
        String title = formField.title().isEmpty() ? field.getName() : formField.title();
        if (!formField.enabled()) {
            title += "(不可编辑)";
        }
        add(new H3(title));
        if (formField.description() != null && !formField.description().isEmpty()) {
            add(new Span(formField.description()));
        }
    }

    /**
     * 初始化数据。
     * 如果 autoInitialize 为 true，则根据表单模型或默认值初始化。
     * 子类可以重写，但必须调用super.initData()。
     */
    protected void initData() {
        if (this.autoInitialize) {
            setData(getFieldDefaultValue());
        }
    }

    /**
     * 将当前数据刷新到UI控件上。
     * 要求幂等，多次调用同样数据不会导致异常。
     */
    abstract public void pushViewData();

    /**
     * 获取当前组件持有的数据。
     * 不涉及UI控件。
     */
    public abstract Type getData();

    /**
     * 设置当前组件持有的数据。
     * 不涉及UI控件。
     */
    public abstract void setData(Type data);

    /**
     * 获取字段默认值。
     * 优先取模型已有值，如果没有则用注解配置的defaultValue，
     * 最后如果还没有，返回一个类型安全的默认值（如null、0等）。
     */
    @SuppressWarnings("unchecked")
    public Type getFieldDefaultValue() {
        Type data = getModelFieldData();
        if (data != null) return data;

        FormField formField = getFormField();
        if (!formField.defaultValue().isEmpty()) {
            return (Type) TypeUtil.convert(formField.defaultValue(), field.getType(), formField.subType());
        }

        return (Type) defaultIfNull(null, field.getType());
    }

    /**
     * 从表单模型中反射获取当前字段的值。
     */
    @SuppressWarnings("unchecked")
    public Type getModelFieldData() {
        getField().setAccessible(true);
        try {
            return (Type) getField().get(this.getFormModel());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将当前组件数据推回表单模型。
     */
    public void invokeModelFileData() {
        getField().setAccessible(true);
        try {
            getField().set(this.getFormModel(), getData());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 清空数据并刷新UI。
     * 如果autoInitialize为true，会重新初始化数据。
     */
    public void clear() {
        clearData();
        clearUI();
        if (autoInitialize) {
            setData(getFieldDefaultValue());
        }
    }

    /**
     * 清空数据，不操作UI。
     */
    abstract public void clearData();

    /**
     * 清空UI控件显示，不操作数据。
     */
    public abstract void clearUI();

    /**
     * 校验当前字段数据。
     * 如果校验失败，显示错误信息。
     * 成功时清除错误信息。
     */
    public Boolean valid() {
        String valid = validField(field, formModel);
        if (valid != null && !valid.isEmpty()) {
            if (errorMessage == null) {
                errorMessage = new ErrorMessage(valid);
                add(errorMessage);
            } else {
                errorMessage.setText(valid);
            }
            return false;
        } else if (errorMessage != null) {
            remove(errorMessage);
        }
        return true;
    }
}
