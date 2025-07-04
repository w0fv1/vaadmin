package dev.w0fv1.vaadmin.test;

import dev.w0fv1.vaadmin.view.table.model.BaseEntityTableModel;
import dev.w0fv1.vaadmin.view.table.model.JsonTableField;
import dev.w0fv1.vaadmin.view.table.model.TableConfig;
import dev.w0fv1.vaadmin.view.table.model.TableField;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static dev.w0fv1.vaadmin.util.JsonUtil.toHashMap;

@Slf4j
@Data
@ToString
@TableConfig(title = "回声管理", description = "这是一个测试页面, 用于测试后台框架的建立", likeSearch = true, pageSize = 20)
@NoArgsConstructor
public class EchoT implements BaseEntityTableModel<Echo, Long> {
    @TableField(displayName = "ID", order = 1, sortable = true, likeSearch = true,frozen = true)
    private Long id;
    @TableField(displayName = "信息", order = 2, likeSearch = true)
    private String message;
    private String longMessage;
    private List<String> keywords;
    private Boolean flag;
    private List<Long> manyToManyEchoesIds = new ArrayList<>();
    @JsonTableField
    private HashMap<String, Object> manyToOneEcho;

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
        echoF.setEchoId(echoId);
        echoF.setStatus(status);
        echoF.setManyToManyEchoes(manyToManyEchoesIds);
        return echoF;
    }

    @Override
    public void formEntity(Echo entity) {
        this.id = entity.getId();
        this.message = entity.getMessage();
        this.longMessage = entity.getLongMessage();
        this.keywords = entity.getKeywords();
        if (entity.getManyToOneEcho() != null) {
            this.manyToOneEcho = toHashMap(entity.getManyToOneEcho());
        }
        this.manyToManyEchoesIds = entity.getManyToManyEchoes().stream().map(Echo::getId).collect(Collectors.toList());
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
