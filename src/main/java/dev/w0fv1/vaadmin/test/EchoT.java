package dev.w0fv1.vaadmin.test;

import dev.w0fv1.vaadmin.view.model.table.BaseManageEntityTableModel;
import dev.w0fv1.vaadmin.view.model.table.TableConfig;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@TableConfig(title = "回声管理", description = "这是一个测试页面, 用于测试后台框架的建立", likeSearch = true)
@NoArgsConstructor
public class EchoT implements BaseManageEntityTableModel<Echo, Long> {
    private Long id;
    private String message;

    @Override
    public EchoF toFormModel() {
        EchoF echoF = new EchoF();
        echoF.setId(id);
        echoF.setMessage(message);

        return echoF;
    }

    @Override
    public void formEntity(Echo entity) {
        this.id = entity.getId();
        this.message = entity.getMessage();
    }

}
