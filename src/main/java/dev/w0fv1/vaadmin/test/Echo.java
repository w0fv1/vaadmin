package dev.w0fv1.vaadmin.test;

import dev.w0fv1.vaadmin.entity.BaseManageEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.util.UUID.randomUUID;


@Getter
@Setter
@NoArgsConstructor
@Entity

@Table(name = "echo")
public class Echo implements BaseManageEntity<Long> {
    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "uuid", unique = true, length = 256)
    private String uuid = randomUUID().toString();

    @Column(name = "message")
    private String message;

    @Column(name = "flag")
    private Boolean flag;

    @Convert(converter = StringListConverter.class)
    private List<String> keywords = new ArrayList<>();

    @ElementCollection(targetClass = Label.class)
    @CollectionTable(name = "echo_labels", joinColumns = @JoinColumn(name = "echo_id"))
    @Enumerated(EnumType.STRING)
    private List<Label> labels = new ArrayList<>();

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status status = Status.NORMAL;

    @Column(name = "created_time")
    @CreationTimestamp
    private OffsetDateTime createdTime;

    @Column(name = "updated_time")
    @UpdateTimestamp
    private OffsetDateTime updatedTime;

    // 多对一关系
    @ManyToOne
    @JoinColumn(name = "many_to_one_echo_id")
    private Echo manyToOneEcho;

    // 一对多关系
    @OneToMany(mappedBy = "manyToOneEcho", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Echo> oneToManyEchoes = new ArrayList<>();

    // 多对多关系
    @ManyToMany
    @JoinTable(
            name = "echo_many_to_many",
            joinColumns = @JoinColumn(name = "echo_id"),
            inverseJoinColumns = @JoinColumn(name = "related_echo_id")
    )
    private List<Echo> manyToManyEchoes = new ArrayList<>();

    public Echo(String message) {
        this.message = message;
    }
    @Converter
    public static class StringListConverter implements AttributeConverter<List<String>, String> {
        @Override
        public String convertToDatabaseColumn(List<String> strings) {
            if (strings == null){
                return "";
            }
            if (strings.isEmpty()){
                return "";
            }
            StringBuilder result = new StringBuilder();
            for (String string : strings){
                result.append(string).append(";");
            }
            return result.substring(0, result.length()-1);
        }

        @Override
        public List<String> convertToEntityAttribute(String s) {
            return List.of(s.split(";"));
        }

    }
    public enum Label {
        NEW,
        HOT,
        RECOMMENDED,

    }

    public enum Status {

        NORMAL("该状态将导致数据处于正常工作状态"),
        HIDDEN("该状态将导致数据隐藏,对用户不可见"),
        BANNED("该状态将导致数据对用户可见,并且显示封禁状态"),
        DELETED("该状态将导致数据被删除,会带来一系列后果!请尽量使用HIDDEN代替.");


        Status(String description) {
            this.description = description;
        }

        public Boolean isNormal() {
            return this == NORMAL;
        }

        public Boolean isNonNormal() {
            return this != NORMAL;
        }

        public Boolean isBanned() {
            return this == BANNED;
        }

        public Boolean isRecHidden() {
            return this != NORMAL;
        }

        public boolean isDeleted() {
            return this == DELETED;
        }

        public final String description;
    }

}
