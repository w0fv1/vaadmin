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
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;

import static dev.w0fv1.vaadmin.component.FieldValidator.validField;
import static dev.w0fv1.vaadmin.util.TypeUtil.defaultIfNull;
import static dev.w0fv1.vaadmin.util.TypeUtil.isEmpty;

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
    @Setter
    private BaseFormModel formModel; // 表单数据模型
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
        logDebug("开始初始化组件");
        initStaticView();
        initData();
        logDebug("数据初始化后，当前值：{}", getData());
        pushViewData();
        logDebug("UI推送数据完成，当前显示值：{}", getData());
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
        logDebug("标题和描述信息构建完成: {}", title);
    }

    /**
     * 初始化数据。
     * 如果 autoInitialize 为 true，则根据表单模型或默认值初始化。
     * 子类可以重写，但必须调用super.initData()。
     */
    protected void initData() {
        if (this.autoInitialize && isEmpty(getData())) {
            logDebug("初始化数据前，当前值为空，准备设置默认值");
            setData(getFieldDefaultValue());
            logDebug("设置默认值后，当前值：{}", getData());
        } else {
            logDebug("跳过数据初始化，已有数据：{}", getData());
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

        if (data != null) {
            logDebug("从表单模型读取到已有数据：{}", data);
            return data;
        }

        FormField formField = getFormField();
        if (!formField.defaultValue().isEmpty()) {
            logDebug("从FormField注解读取到默认值：{}", formField.defaultValue());
            return (Type) TypeUtil.convert(formField.defaultValue(), field.getType(), formField.subType());
        }

        logDebug("无模型值、无注解默认值，使用类型安全默认值");
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
            logDebug("将当前数据推回模型：{}", getData());
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
        logDebug("开始清空组件数据和UI");
        clearData();
        clearUI();
        clearValid();
        if (autoInitialize) {
            logDebug("启用autoInitialize，重新初始化数据");
            initData();
            pushViewData();
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

    public void clearValid() {
        if (errorMessage != null) {
            logDebug("清除错误提示信息");
            errorMessage.setText("");
            remove(errorMessage);
        }
    }

    /**
     * 校验当前字段数据。
     * 如果校验失败，显示错误信息。
     * 成功时清除错误信息。
     */
    public Boolean valid() {
        logDebug("开始执行字段校验");
        String validMessage = "";

        if (field.isAnnotationPresent(FormField.class)) {
            FormField formField = field.getAnnotation(FormField.class);
            if (formField != null && !formField.nullable() && isEmpty(getData())) {
                validMessage = "值为空，该字段不允许为空";
                logDebug("字段不允许为空校验失败");
            }
        }

        if (validMessage.isEmpty()) {
            validMessage = validField(field, formModel);
        }

        if (validMessage != null && !validMessage.isEmpty()) {
            log.warn("字段 [{}] 校验失败: {}", field.getName(), validMessage);
            if (errorMessage == null) {
                errorMessage = new ErrorMessage(validMessage);
                add(errorMessage);
            } else {
                errorMessage.setText(validMessage);
            }
            return false;
        } else {
            logDebug("字段校验通过，数据为：{}", getData());
            if (errorMessage != null) {
                remove(errorMessage);
            }
            return true;
        }
    }

    /**
     * 统一的 debug 日志方法，自动附加字段名。
     *
     * @param message 日志信息模板
     * @param args    参数列表
     */
    protected void logDebug(String message, Object... args) {
        if (log.isDebugEnabled()) {
            // 创建新的数组，fieldName + 原来的 args
            Object[] newArgs = new Object[args.length + 1];
            newArgs[0] = field.getName();
            System.arraycopy(args, 0, newArgs, 1, args.length);

            log.debug("[{}] " + message, newArgs);
        }
    }


}
