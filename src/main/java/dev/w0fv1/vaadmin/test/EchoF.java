package dev.w0fv1.vaadmin.test;

import dev.w0fv1.vaadmin.view.form.FormFieldComponent;
import dev.w0fv1.vaadmin.view.form.component.SampleFileUploadFieldComponent;
import dev.w0fv1.vaadmin.view.form.component.SampleFormFieldComponentBuilder;
import dev.w0fv1.vaadmin.view.model.form.*;
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
    @FormField(title = "信息")
    private String message;

    @NotBlank(message = "消息不能为空")
    @Size(min = 10, max = 1255, message = "消息长度必须在10到255个字符之间")
    @FormField(title = "长信息")
    private String longMessage;

    @NotBlank(message = "消息不能为空")
    @Size(min = 10, max = 1255, message = "消息长度必须在10到255个字符之间")
    @FormField(title = "默认信息", defaultValue = "默认信息")
    private String defaultMessage;

    @FormField(title = "默认Bool", defaultValue = "true")
    private Boolean defaultBooleanMessage;


    @FormFieldComponent(SampleFormFieldComponentBuilder.class)
    @NotBlank(message = "消息不能为空")
    @Size(min = 10, max = 1255, message = "消息长度必须在10到255个字符之间")
    @FormField(title = "定制化信息组件")
    private String customMessage;

    @FormFieldComponent(SampleFileUploadFieldComponent.SampleFileFormFieldComponentBuilder.class)
    @FormField(title = "上传文件示例")
    private String fileUrl;

    @FormField(title = "关键词", subType = String.class)
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

    @FormEntitySelectField(
           entityField = @EntityField(
                   entityMapper = EchoEntityFieldMapper.ManyToOneEchoFieldMapper.class,
                   entityType = Echo.class)
    )
    @FormField(title = "manyToOne回声")
    private Long echoId;

    @EntityField(
            entityMapper = EchoEntityFieldMapper.ManyToOneEchoFieldMapper.class,
            entityType = Echo.class)
    @FormField(title = "手动manyToOne回声")
    private Long manualEchoId;

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
