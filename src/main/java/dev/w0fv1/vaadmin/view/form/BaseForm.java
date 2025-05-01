package dev.w0fv1.vaadmin.view.form;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
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

    public void build() {
        buildTitle();
        buildDataForm();
        buildAction();
    }

    private void buildAction() {
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
        for (BaseFormFieldComponent<?> fieldComponent : fieldComponents) {
            fieldComponent.setModelData();
        }
        List<Boolean> validResults = new ArrayList<>();
        for (BaseFormFieldComponent<?> fieldComponent : fieldComponents) {
            Boolean valid = fieldComponent.valid();
            validResults.add(valid);
        }
        for (Boolean validResult : validResults) {
            if (!validResult) {
                return;
            }
        }

        for (BaseFormFieldComponent<?> fieldComponent : fieldComponents) {
            fieldComponent.save();
        }
        // ------ 在校验完之后做自定义的字符串处理 ------
        handleTextTransform();

        log.info(model.toString());

        this.onSave(model);

        this.clear();
    }

    abstract public void onSave(F data);

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
            fieldComponent.clear(this.model);
        }
    }


    abstract public void onCancel();

    private void buildDataForm() {
        List<Field> fieldList = getAllFields(fromClass, ReflectionUtils.withModifier(Modifier.PRIVATE)).stream().toList();
        log.info("Field List的数量为{}", fieldList.size());
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
                formFieldInfo = new FormField.DefaultFormField();
            }
            if (!formFieldInfo.display()){
                continue;
            }

            if (!isUpdate && (formFieldInfo.onlyUpdate() || formFieldInfo.id())) {
                log.info("该Field {} 为仅更新, 在创建数据时不建造Field", field.getName());
                continue;
            }

            add(mapComponent(field));
        }
    }

    private Component mapComponent(Field field) {
        BaseFormFieldComponent<?> formFieldComponent = this.extMapComponent(field, this.model);
        if (formFieldComponent != null) {
            fieldComponents.add(formFieldComponent);
            return formFieldComponent;
        }

        Class<?> type = field.getType();
        FormField fromField = field.getAnnotation(FormField.class);

        if (fromField == null) {
            log.info(field.getName());
        }

        if (field.isAnnotationPresent(CustomFormFieldComponent.class)) {
            Class<? extends CustomFormFieldComponentBuilder> fieldComponentBuilder = field.getAnnotation(CustomFormFieldComponent.class).value();

            try {
                CustomFormFieldComponentBuilder customFormFieldComponentBuilder = fieldComponentBuilder.getDeclaredConstructor().newInstance();
                formFieldComponent = customFormFieldComponentBuilder.build(field, model);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        } else if (type.equals(String.class) && (!field.isAnnotationPresent(Size.class) || (field.getAnnotation(Size.class).max() < 256))) {
            formFieldComponent = new TextInputField(field, model);
        } else if (type.equals(String.class) && (field.isAnnotationPresent(Size.class) && (field.getAnnotation(Size.class).max() > 256))) {
            formFieldComponent = new LongTextInputField(field, model);
        } else if (fromField.id() && type.equals(String.class)) {
            formFieldComponent = new StringIdField(field, model);
        } else if (fromField.id() && type.equals(Long.class)) {
            formFieldComponent = new LongIdField(field, model);
        } else if (type.equals(BigDecimal.class)) {
            formFieldComponent = new BigDecimalInputField(field, model);
        } else if (type.equals(Double.class)) {
            formFieldComponent = new NumberInputField<>(field, model, Double.class);
        } else if (type.equals(Float.class)) {
            formFieldComponent = new NumberInputField<>(field, model, Float.class);
        } else if (type.equals(Long.class)) {
            formFieldComponent = new NumberInputField<>(field, model, Long.class);
        } else if (type.equals(Integer.class)) {
            formFieldComponent = new NumberInputField<>(field, model, Integer.class);
        } else if (type.equals(Short.class)) {
            formFieldComponent = new NumberInputField<>(field, model, Short.class);
        } else if (type.equals(Byte.class)) {
            formFieldComponent = new NumberInputField<>(field, model, Byte.class);
        } else if (type.equals(Boolean.class)) {
            formFieldComponent = new BooleanCheckBoxField(field, model);
        } else if (type.equals(List.class) && fromField.subType().equals(String.class)) {
            formFieldComponent = new TagInputField(field, model);
        } else if (type.equals(List.class) && fromField.subType().isEnum()) {
            formFieldComponent = new MultiEnumSelectField(field, model);
        } else if (type.isEnum()) {
            formFieldComponent = new SingleEnumSelectBoxField(field, model);
        } else if (type.equals(OffsetDateTime.class)) {
            formFieldComponent = new DateTimeField(field, model);
        }
        if (formFieldComponent == null) {
            throw new IllegalStateException("formFieldComponent为null, 这是不应该发生的, 请检查 " + field.getName() + " 字段的类型");
        }

        fieldComponents.add(formFieldComponent);
        return formFieldComponent;
    }

    BaseFormFieldComponent<?> extMapComponent(Field field, F formModel) {
        return null;
    }


    private void buildTitle() {
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.add(new H1(getTitle()));
        horizontalLayout.add(new Span(getDescription()));
        horizontalLayout.setAlignItems(Alignment.END);
        horizontalLayout.add(extTitle());
        add(horizontalLayout);
    }

    public void setDefaultModel(F defaultModel) {
        this.defaultModel = defaultModel;
        clear();
        log.info("defaultModel：{}", defaultModel);
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
