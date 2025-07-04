package dev.w0fv1.vaadmin.test;

import dev.w0fv1.vaadmin.GenericRepository;
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
    @FormField(title = "信息", nullable = false)
    private String message;

    @FormField(title = "长信息", nullable = false)
    private String longMessage;


    @FormEntitySelectField(
            entityField = @EntityField(entityType = Echo.class, entityMapper = EchoEntityFieldMapper.ManyToOneEchoFieldMapper.class)
    )
    @FormField(title = "多对一", nullable = false)
    private Long echoId;


    @FormEntitySelectField(
            entityField = @EntityField(entityType = Echo.class, entityMapper = EchoEntityFieldMapper.ManyToManyEchoesFieldMapper.class)
    )
    @FormField(title = "多对多", nullable = false)
    private List<Long> manyToManyEchoes;

    @FormField(title = "关键词", nullable = false, subType = String.class)
    private List<String> keywords;


    @CustomFormFieldComponent(UserverFileUploadFieldComponent.UserverFileFormFieldComponentBuilder.class)
    @FormField(title = "商品主图")
    private String imageUrl;
    @FormField(title = "状态", nullable = false)
    private Echo.Status status;

    public EchoF(String message) {
        this.message = message;
    }


    public EchoF(Echo.Status status) {
        this.status = status;
    }

    @Override
    public Echo toEntity() {
        Echo echo = new Echo();
        echo.setMessage(message);
        echo.setLongMessage(longMessage);
        echo.setKeywords(keywords);
        echo.setStatus(status);
        return echo;
    }

    @Override
    public void translate(Echo model) {
        model.setMessage(message);
        model.setLongMessage(longMessage);
        model.setKeywords(keywords);
        model.setStatus(status);
    }

    @Override
    public GenericRepository.PredicateBuilder<Echo> getEntityPredicateBuilder() {
        // 永久过滤：status == NORMAL
        return (cb, root, predicates) -> predicates.add(
                cb.equal(root.get("status"), getStatus())
        );
    }
}
