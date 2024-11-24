package dev.w0fv1.vaadmin.view.form.component;

import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import dev.w0fv1.vaadmin.view.model.form.BaseFormModel;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Slf4j
@Getter
public class DateTimeField extends BaseFormFieldComponent<OffsetDateTime> {
    @NotNull
    private final DateTimePicker dateTimePicker;

    public DateTimeField(Field field, BaseFormModel formModel) {
        super(field, formModel);


        this.dateTimePicker = new DateTimePicker();
        this.dateTimePicker.setId(field.getName()); // 设置唯一的 fieldId

        this.dateTimePicker.setStep(Duration.ofMinutes(30)); // Set step to 30 minutes

        OffsetDateTime modelData = getModelData();
        if (modelData != null) {
            this.dateTimePicker.setValue(modelData.toLocalDateTime());
        }

        this.dateTimePicker.setEnabled(getFormField().enabled());

        this.add(this.dateTimePicker);
    }



    @Override
    public OffsetDateTime getData() {
        LocalDateTime localDateTime = this.dateTimePicker.getValue();
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.atOffset(ZoneOffset.UTC);
    }
    @Override
    public void setData(OffsetDateTime data) {
        this.dateTimePicker.setValue(data.toLocalDateTime());
    }
    @Override
    public void clear() {
        this.dateTimePicker.clear();
        this.dateTimePicker.setValue(LocalDateTime.now());
    }
}
