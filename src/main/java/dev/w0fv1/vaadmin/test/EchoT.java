package dev.w0fv1.vaadmin.test;

import dev.w0fv1.vaadmin.view.model.table.BaseEntityTableModel;
import dev.w0fv1.vaadmin.view.model.table.TableConfig;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@ToString
@TableConfig(title = "回声管理", description = "这是一个测试页面, 用于测试后台框架的建立", likeSearch = true)
@NoArgsConstructor
public class EchoT implements BaseEntityTableModel<Echo, Long> {
    private Long id;
    private String message;
    private String longMessage;
    private List<String> keywords;
    private Boolean flag;
    private List<Echo.Label> labels;
    private Echo.Status status;
    private OffsetDateTime createdTime;

    private OffsetDateTime updatedTime;

    private Long echoId;

    @Override
    public EchoF toFormModel() {
        EchoF echoF = new EchoF();
        echoF.setId(id);
        echoF.setMessage(message);
        echoF.setLongMessage(longMessage);
        echoF.setKeywords(keywords);


        echoF.setFlag(flag);
        echoF.setLabels(labels);
        echoF.setDefaultMessage(longMessage);
        echoF.setCustomMessage(message);
        echoF.setStatus(status);
        echoF.setCreatedTime(createdTime);
        echoF.setUpdatedTime(updatedTime);
        echoF.setEchoId(echoId);
        return echoF;
    }

    @Override
    public void formEntity(Echo entity) {
        this.id = entity.getId();
        this.message = entity.getMessage();
        this.longMessage = entity.getMessage();
        this.keywords = entity.getKeywords();
        this.flag = entity.getFlag();
        this.labels = entity.getLabels();
        this.status = entity.getStatus();
        this.createdTime = entity.getCreatedTime();
        this.updatedTime = entity.getUpdatedTime();
        if (entity.getManyToOneEcho() != null) {
            this.echoId = entity.getManyToOneEcho().getId();
        }
    }

}
