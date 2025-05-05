package dev.w0fv1.vaadmin.test;

import dev.w0fv1.vaadmin.view.form.RepositoryMapField;
import dev.w0fv1.vaadmin.view.form.component.SampleRepositoryDialogFormFieldComponent;
import dev.w0fv1.vaadmin.view.form.model.*;
import dev.w0fv1.vaadmin.view.form.UpperCaseConverter;
import dev.w0fv1.vaadmin.view.form.component.SampleDialogFormFieldComponent;
import dev.w0fv1.vaadmin.view.form.component.SampleBaseFileUploadFieldComponent;
import dev.w0fv1.vaadmin.view.form.component.SampleFormFieldComponentBuilder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@ToString
@NoArgsConstructor
@FormConfig(title = "回声表单")
public class EchoF implements BaseFormModel, BaseEntityFormModel<Echo, Long> {

    @FormField(id = true)
    private Long id;

    @NotBlank(message = "消息不能为空")
    @Size(min = 10, max = 255, message = "消息长度必须在10到255个字符之间")
    @FormField(title = "信息",nullable = false)
    private String message;

    @FormField(title = "长信息")
    private String longMessage;

    @TextTransform(processorClass = UpperCaseConverter.class)
    @FormField(title = "变化信息")
    private String transformedMessage;

    @FormField(title = "默认信息", defaultValue = "默认信息")
    private String defaultMessage;

    @FormField(title = "默认Bool", defaultValue = "true")
    private Boolean defaultBooleanMessage;


    @CustomFormFieldComponent(SampleFormFieldComponentBuilder.class)
    @FormField(title = "定制化信息组件")
    private String customMessage;

    @CustomFormFieldComponent(SampleBaseFileUploadFieldComponent.SampleFileUploadFieldComponentBuilder.class)
    @FormField(title = "上传文件示例")
    private String fileUrl;


    @CustomFormFieldComponent(SampleDialogFormFieldComponent.SampleDialogFormFieldComponentBuilder.class)
    @FormField(title = "dialog示例")
    private String dialog;

    @CustomRepositoryFormFieldComponent(SampleRepositoryDialogFormFieldComponent.SampleRepositoryDialogFormFieldComponentBuilder.class)
    @FormField(title = "repository dialog示例")
    private Long repositoryDialogId;


    @FormField(title = "关键词", subType = String.class, defaultValue = "[\"TEST\"]")
    private List<String> keywords;

    @FormField(title = "标记")
    private Boolean flag;

    @FormField(title = "标签", subType = Echo.Label.class)
    private List<Echo.Label> labels;

    @FormField(title = "状态")
    private Echo.Status status;

    @FormField(title = "创建时间")
    private OffsetDateTime createdTime;

    @FormField(title = "更新时间")
    private OffsetDateTime updatedTime;

    @FormField(title = "隐藏不显示的field",display = false)
    private String hide;

    @FormEntitySelectField(
            entityField = @EntityField(
                    entityMapper = EchoEntityFieldMapper.ManyToOneEchoFieldMapper.class,
                    entityType = Echo.class)
    )
    @FormField(title = "manyToOne回声")
    private Long echoId;

    @RepositoryMapField(mapper = IdEchoRepositoryMapper.class)
    @FormField(title = "manyToOne回声Mapper")
    private Long convertEchoId;

    @EntityField(
            entityMapper = EchoEntityFieldMapper.ManyToOneEchoFieldMapper.class,
            entityType = Echo.class)
    @FormField(title = "手动manyToOne回声", enabled = false)
    private Long manualEchoId;

    public EchoF(String message) {
        this.message = message;
    }

    @Override
    public Echo toEntity() {
        Echo echo = new Echo();
        echo.setMessage(message);
        echo.setFlag(flag);
        echo.setKeywords(keywords);
        echo.setLabels(labels);
        echo.setStatus(status);
        echo.setCreatedTime(createdTime);
        echo.setUpdatedTime(updatedTime);
        System.out.println(this.toString());
        return echo;
    }

    @Override
    public void translate(Echo model) {
        model.setMessage(message);
        model.setFlag(flag);
        model.setKeywords(keywords);
        model.setLabels(labels);
        model.setStatus(status);
    }


}
