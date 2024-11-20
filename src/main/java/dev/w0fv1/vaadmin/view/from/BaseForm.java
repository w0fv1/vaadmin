package dev.w0fv1.vaadmin.view.from;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import dev.w0fv1.vaadmin.view.model.form.BaseFormModel;
import dev.w0fv1.vaadmin.view.model.form.FormConfig;
import dev.w0fv1.vaadmin.view.model.form.FormField;
import dev.w0fv1.vaadmin.view.from.component.*;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import org.reflections.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.OffsetDateTime;
import java.util.*;

import static org.reflections.ReflectionUtils.getAllFields;


@Slf4j
public abstract class BaseForm<F extends BaseFormModel> extends VerticalLayout {
    private final Class<F> fromClass;
    private final FormConfig formConfig;

    private final F formModel;

    private final Boolean isUpdate;

    private final List<BaseFormField<?>> fieldComponents = new ArrayList<>();

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
        this.formModel = fromModel;
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
        for (BaseFormField<?> fieldComponent : fieldComponents) {
            fieldComponent.setModelData();
        }
        List<Boolean> validResults = new ArrayList<>();
        for (BaseFormField<?> fieldComponent : fieldComponents) {
            Boolean valid = fieldComponent.valid();
            validResults.add(valid);
        }
        for (Boolean validResult : validResults) {
            if (!validResult) {
                return;
            }
        }

        for (BaseFormField<?> fieldComponent : fieldComponents) {
            fieldComponent.save();
        }
        log.info(formModel.toString());

        this.onSave(formModel);
    }

    abstract public void onSave(F data);

    private void clear() {
        for (BaseFormField<?> fieldComponent : fieldComponents) {
            fieldComponent.clear();
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

            if (!isUpdate && (formFieldInfo.onlyUpdate() || formFieldInfo.id())) {
                log.info("该Field {} 为仅更新, 在创建数据时不建造Field", field.getName());
                continue;
            }

            add(mapComponent(field));
        }
    }

    private Component mapComponent(Field field) {
        BaseFormField<?> formFieldComponent = this.extMapComponent(field, this.formModel);
        if (formFieldComponent != null) {
            fieldComponents.add(formFieldComponent);
            return formFieldComponent;
        }

        Class<?> type = field.getType();
        FormField fromField = field.getAnnotation(FormField.class);
        if (type.equals(String.class) &&(!field.isAnnotationPresent(Size.class) || (field.getAnnotation(Size.class).max() < 256))) {
            formFieldComponent = new TextInputField(field, formModel);
        } else if (type.equals(String.class) && (field.isAnnotationPresent(Size.class) && (field.getAnnotation(Size.class).max() > 256))) {
            formFieldComponent = new LongTextInputField(field, formModel);
        } else if (type.equals(Double.class)) {
            formFieldComponent = new NumberInputField(field, formModel);
        } else if (type.equals(Boolean.class)) {
            formFieldComponent = new BooleanCheckBoxField(field, formModel);
        } else if (type.equals(List.class) && fromField.subType().equals(String.class)) {
            formFieldComponent = new TagInputField(field, formModel);
        } else if (type.equals(List.class) && fromField.subType().isEnum()) {
            formFieldComponent = new MultiEnumSelectField(field, formModel);
        } else if (type.isEnum()) {
            formFieldComponent = new SingleEnumSelectBoxField(field, formModel);
        } else if (type.equals(OffsetDateTime.class)) {
            formFieldComponent = new DateTimeField(field, formModel);
        }
        if (formFieldComponent == null) {
            throw new IllegalStateException("formFieldComponent为null, 这是不应该发生的, 请检查 " + field.getName() + " 字段的类型");
        }

        fieldComponents.add(formFieldComponent);
        return formFieldComponent;
    }

    abstract BaseFormField<?> extMapComponent(Field field, F formModel);

    private void buildTitle() {
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.add(new H1(getTitle()));
        horizontalLayout.add(new Span(getDescription()));
        horizontalLayout.setAlignItems(Alignment.END);
        horizontalLayout.add(extTitle());
        add(horizontalLayout);
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
