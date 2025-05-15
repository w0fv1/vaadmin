package dev.w0fv1.vaadmin.test;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.w0fv1.vaadmin.entity.BaseManageEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

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

    @Column(name = "long_message")
    private String longMessage;

    @Column(name = "flag")
    private Boolean flag;

    @Convert(converter = StringListConverter.class)
    private List<String> keywords = new ArrayList<>();

    @ElementCollection(targetClass = Label.class, fetch = FetchType.EAGER)
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

    @JsonIgnore
    // 多对一关系
    @ManyToOne
    @JoinColumn(name = "many_to_one_echo_id")
    private Echo manyToOneEcho;
    @JsonIgnore
    // 一对多关系
    @OneToMany(mappedBy = "manyToOneEcho", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Echo> oneToManyEchoes = new ArrayList<>();
    @JsonIgnore

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
        private static final String SEPARATOR = ";";

        @Override
        public String convertToDatabaseColumn(List<String> attribute) {
            if (attribute == null || attribute.isEmpty()) {
                return ""; // 或者返回 null，取决于你的数据库列是否允许 NULL 和你的偏好
                // 如果返回 null，convertToEntityAttribute 也需要处理 null
            }
            // 过滤掉列表中的 null 或空字符串，避免产生 "a;;b" 这样的情况，除非你特意需要
            // return attribute.stream()
            //                 .filter(s -> s != null && !s.isEmpty())
            //                 .collect(Collectors.joining(SEPARATOR));
            return String.join(SEPARATOR, attribute); // String.join 会处理 null 元素，但可能不是你想要的方式
        }

        @Override
        public List<String> convertToEntityAttribute(String dbData) {
            if (dbData == null || dbData.isEmpty()) {
                return new ArrayList<>(); // <--- 关键修改：返回一个空的可变列表
            }
            // 使用 Arrays.asList 然后包装成 new ArrayList 确保返回的是可变列表
            return new ArrayList<>(Arrays.asList(dbData.split(Pattern.quote(SEPARATOR))));
            // Pattern.quote(SEPARATOR) 是为了防止SEPARATOR是正则表达式特殊字符时产生问题
            // 如果你确定SEPARATOR永远是简单字符，直接用 dbData.split(SEPARATOR) 也可以
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
