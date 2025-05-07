package dev.w0fv1.vaadmin.view.form;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import dev.w0fv1.vaadmin.view.form.model.*;
import dev.w0fv1.vaadmin.view.form.component.*;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import org.reflections.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

import static dev.w0fv1.vaadmin.view.tools.Notifier.showNotification;
import static org.reflections.ReflectionUtils.getAllFields;


@Slf4j
public abstract class BaseForm<F extends BaseFormModel> extends VerticalLayout {
    private final Class<F> fromClass;
    private final FormConfig formConfig;

    private F model;

    private F defaultModel;

    private final Boolean isUpdate;

    private final List<BaseFormFieldComponent<?>> fieldComponents = new ArrayList<>();

    protected BaseForm(F fromModel, Boolean isUpdate) {
        if (fromModel == null) {
            throw new NullPointerException("表单model不能为null, 如果是创建可以输入无数据的空对象");
        }
        this.fromClass = (Class<F>) fromModel.getClass();
        if (!fromClass.isAnnotationPresent(FormConfig.class)) {
            throw new IllegalStateException("表单class未集成@FromConfig.class");
        }
        this.formConfig = fromClass.getAnnotation(FormConfig.class);

        this.isUpdate = isUpdate;
        this.defaultModel = fromModel;
        this.model = defaultModel.copy();
        this.setPadding(false);
    }

    public void initialize() {
        initTitle();
        initDataForm();
        initAction();
    }

    private void initAction() {
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        Button saveButton = new Button("保存");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(v -> save());
        horizontalLayout.add(saveButton);


        if (!isUpdate) {
            Button clearButton = new Button("清空");
            clearButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            clearButton.addClickListener(v -> clear());
            horizontalLayout.add(clearButton);
        }

        Button cancelButton = new Button("取消");
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancelButton.addClickListener(v -> {
            clear();
            onCancel();
        });
        horizontalLayout.add(cancelButton);

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.add(horizontalLayout);
        verticalLayout.add(extAction());
        verticalLayout.setPadding(false);
        add(verticalLayout);
    }


    private void save() {

        List<Boolean> validResults = new ArrayList<>();

        for (BaseFormFieldComponent<?> fieldComponent : fieldComponents) {
            fieldComponent.clearValid();
        }

        for (BaseFormFieldComponent<?> fieldComponent : fieldComponents) {
            Boolean valid = fieldComponent.valid();
            validResults.add(valid);
        }
        log.debug("validResults: {}", Arrays.toString(validResults.toArray()));
        for (Boolean validResult : validResults) {
            if (!validResult) {
                showNotification("表单字段存在错误，请按说明修正", NotificationVariant.LUMO_ERROR);
                return;
            }
        }

        for (BaseFormFieldComponent<?> fieldComponent : fieldComponents) {
            fieldComponent.invokeModelFileData();
        }
        // ------ 在校验完之后做自定义的字符串处理 ------
        handleTextTransform();

        log.info(model.toString());

        Boolean onSaveResult = this.onSave(model);

        if (onSaveResult) {
            this.clear();
        }
    }

    abstract public Boolean onSave(F data);

    /**
     * 针对标记了 @TextTransform 的字段进行处理
     */
    private void handleTextTransform() {
        Class<F> clazz = this.fromClass;
        // 获取所有字段
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (!field.isAnnotationPresent(TextTransform.class)) {
                continue;
            }
            if (!field.getType().equals(String.class)) {
                // 如果不是String类型，你也可以选择跳过或者抛异常
                continue;
            }

            // 取到注解和对应的处理器
            TextTransform annotation = field.getAnnotation(TextTransform.class);
            Class<? extends StringConverter> transformerClass = annotation.processorClass();

            try {
                StringConverter transformer = transformerClass.getDeclaredConstructor().newInstance();

                // 拿到当前字段的值
                field.setAccessible(true);
                String originalValue = (String) field.get(model);

                // 做转换
                String newValue = transformer.convert(originalValue);

                // 放回model
                field.set(model, newValue);

            } catch (Exception e) {
                log.error("handleTextTransform error: ", e);
                // 你可以选择抛出异常或者忽略
            }
        }
    }

    private void clear() {
        this.model = defaultModel.copy();

        for (BaseFormFieldComponent<?> fieldComponent : fieldComponents) {
            fieldComponent.setFormModel(this.model);
            fieldComponent.clear();
        }
    }


    abstract public void onCancel();

    /**
     * 初始化表单字段数据。
     */
    private void initDataForm() {
        log.debug("开始初始化数据表单，处理 class：{}", fromClass.getSimpleName());

        List<Field> fieldList = getAllFields(fromClass, ReflectionUtils.withModifier(Modifier.PRIVATE)).stream().toList();
        log.info("字段的数量为{}，开始处理字段", fieldList.size());

        fieldList = fieldList.stream()
                .sorted(Comparator.comparingDouble(f -> {
                    FormField annotation = f.getAnnotation(FormField.class);
                    return annotation != null ? annotation.order() : 100;
                }))
                .toList();

        for (Field field : fieldList) {
            field.setAccessible(true); // Allow access to private fields
            FormField formFieldInfo = field.getAnnotation(FormField.class);

            if (formFieldInfo == null) {
                log.debug("字段 [{}] 未标注 FormField 注解，使用默认配置", field.getName());
                formFieldInfo = new FormField.DefaultFormField();
            } else {
                log.debug("字段 [{}] 读取到 FormField 注解配置：{}", field.getName(), formFieldInfo);
            }

            if (!formFieldInfo.display()) {
                log.debug("字段 [{}] 配置为 display=false，跳过构建", field.getName());
                continue;
            }

            if (!isUpdate && (formFieldInfo.onlyUpdate() || formFieldInfo.id())) {
                log.debug("字段 [{}] 配置为仅更新字段（onlyUpdate 或 id），在创建数据时跳过", field.getName());
                continue;
            }

            log.debug("字段 [{}] 符合构建条件，开始生成组件", field.getName());
            add(mapComponent(field));
        }

        log.debug("数据表单初始化完成，处理字段总数：{}", fieldList.size());
    }

    /**
     * 根据字段类型映射生成对应的表单组件。
     *
     * @param field 字段
     * @return 对应的组件
     */
    private Component mapComponent(Field field) {
        log.debug("开始映射字段 [{}] 到表单组件", field.getName());

        BaseFormFieldComponent<?> formFieldComponent = this.extMapComponent(field, this.model);
        if (formFieldComponent != null) {
            log.debug("字段 [{}] 通过 extMapComponent 扩展方法自定义生成了组件：{}", field.getName(), formFieldComponent.getClass().getSimpleName());
            fieldComponents.add(formFieldComponent);
            return formFieldComponent;
        }

        Class<?> type = field.getType();
        FormField fromField = field.getAnnotation(FormField.class);

        if (fromField == null) {
            log.debug("字段 [{}] 没有找到 FormField 注解，使用默认处理", field.getName());
        }

        if (field.isAnnotationPresent(CustomFormFieldComponent.class)) {
            log.debug("字段 [{}] 存在 CustomFormFieldComponent 注解，使用自定义构建器生成组件", field.getName());
            Class<? extends CustomFormFieldComponentBuilder> fieldComponentBuilder = field.getAnnotation(CustomFormFieldComponent.class).value();

            try {
                CustomFormFieldComponentBuilder customFormFieldComponentBuilder = fieldComponentBuilder.getDeclaredConstructor().newInstance();
                formFieldComponent = customFormFieldComponentBuilder.build(field, model);
                log.debug("字段 [{}] 通过自定义构建器生成组件：{}", field.getName(), formFieldComponent.getClass().getSimpleName());
            } catch (Exception e) {
                log.error("字段 [{}] 自定义构建器实例化失败", field.getName(), e);
                throw new RuntimeException(e);
            }

        } else if (type.equals(String.class) && (!field.isAnnotationPresent(Size.class) || (field.getAnnotation(Size.class).max() < 256))) {
            formFieldComponent = new TextInputField(field, model);
            log.debug("字段 [{}] 匹配 TextInputField", field.getName());
        } else if (type.equals(String.class) && (field.isAnnotationPresent(Size.class) && (field.getAnnotation(Size.class).max() > 256))) {
            formFieldComponent = new LongTextInputField(field, model);
            log.debug("字段 [{}] 匹配 LongTextInputField", field.getName());
        } else if (fromField.id() && type.equals(String.class)) {
            formFieldComponent = new StringIdField(field, model);
            log.debug("字段 [{}] 匹配 StringIdField", field.getName());
        } else if (fromField.id() && type.equals(Long.class)) {
            formFieldComponent = new LongIdField(field, model);
            log.debug("字段 [{}] 匹配 LongIdField", field.getName());
        } else if (type.equals(BigDecimal.class)) {
            formFieldComponent = new BigDecimalInputField(field, model);
            log.debug("字段 [{}] 匹配 BigDecimalInputField", field.getName());
        } else if (type.equals(Double.class)) {
            formFieldComponent = new NumberInputField<>(field, model, Double.class);
            log.debug("字段 [{}] 匹配 NumberInputField<Double>", field.getName());
        } else if (type.equals(Float.class)) {
            formFieldComponent = new NumberInputField<>(field, model, Float.class);
            log.debug("字段 [{}] 匹配 NumberInputField<Float>", field.getName());
        } else if (type.equals(Long.class)) {
            formFieldComponent = new NumberInputField<>(field, model, Long.class);
            log.debug("字段 [{}] 匹配 NumberInputField<Long>", field.getName());
        } else if (type.equals(Integer.class)) {
            formFieldComponent = new NumberInputField<>(field, model, Integer.class);
            log.debug("字段 [{}] 匹配 NumberInputField<Integer>", field.getName());
        } else if (type.equals(Short.class)) {
            formFieldComponent = new NumberInputField<>(field, model, Short.class);
            log.debug("字段 [{}] 匹配 NumberInputField<Short>", field.getName());
        } else if (type.equals(Byte.class)) {
            formFieldComponent = new NumberInputField<>(field, model, Byte.class);
            log.debug("字段 [{}] 匹配 NumberInputField<Byte>", field.getName());
        } else if (type.equals(Boolean.class)) {
            formFieldComponent = new BooleanCheckBoxField(field, model);
            log.debug("字段 [{}] 匹配 BooleanCheckBoxField", field.getName());
        } else if (type.equals(List.class) && fromField.subType().equals(String.class)) {
            formFieldComponent = new TagInputField(field, model);
            log.debug("字段 [{}] 匹配 TagInputField<String>", field.getName());
        } else if (type.equals(List.class) && fromField.subType().isEnum()) {
            formFieldComponent = new MultiEnumSelectField(field, model);
            log.debug("字段 [{}] 匹配 MultiEnumSelectField", field.getName());
        } else if (type.isEnum()) {
            formFieldComponent = new SingleEnumSelectBoxField(field, model);
            log.debug("字段 [{}] 匹配 SingleEnumSelectBoxField", field.getName());
        } else if (type.equals(OffsetDateTime.class)) {
            formFieldComponent = new DateTimeField(field, model);
            log.debug("字段 [{}] 匹配 DateTimeField", field.getName());
        }

        if (formFieldComponent == null) {
            log.error("字段 [{}] 未能成功映射到任何组件类型，抛出异常", field.getName());
            throw new IllegalStateException("formFieldComponent为null, 这是不应该发生的, 请检查 " + field.getName() + " 字段的类型");
        }

        fieldComponents.add(formFieldComponent);
        log.debug("字段 [{}] 成功添加到表单组件列表，组件类型：{}", field.getName(), formFieldComponent.getClass().getSimpleName());
        return formFieldComponent;
    }


    BaseFormFieldComponent<?> extMapComponent(Field field, F formModel) {
        return null;
    }


    private void initTitle() {
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.add(new H1(getTitle()));
        horizontalLayout.add(new Span(getDescription()));
        horizontalLayout.setAlignItems(Alignment.END);
        horizontalLayout.add(extTitle());
        add(horizontalLayout);
    }

    /**
     * 设置默认模型并清空当前表单。
     *
     * @param defaultModel 默认模型数据
     */
    public void setDefaultModel(F defaultModel) {
        this.defaultModel = defaultModel;
        clear();
        log.debug("调用 setDefaultModel()，已设置 defaultModel，并清空表单数据，defaultModel 内容：{}", defaultModel);
    }


    private Component extTitle() {
        return new Div();
    }

    private Component extAction() {
        return new Div();
    }

    public String getTitle() {
        return formConfig.title();
    }

    public String getDescription() {
        return formConfig.description();
    }


}
