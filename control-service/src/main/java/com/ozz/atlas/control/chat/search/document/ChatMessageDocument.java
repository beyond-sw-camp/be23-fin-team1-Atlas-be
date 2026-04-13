package com.ozz.atlas.control.chat.search.document;

import com.ozz.atlas.common.domain.DomainType;
import com.ozz.atlas.common.jpa.Status;
import com.ozz.atlas.control.chat.domain.ChatMessage;
import com.ozz.atlas.control.chat.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(indexName = "chat-messages")
@Setting(settingPath = "/elasticsearch/chat-settings.json")
public class ChatMessageDocument {

    @Id
    // Elasticsearch 문서 기본 식별자
    private Long chatMessageId;

    // 메시지 publicId
    @Field(type = FieldType.Keyword)
    private String publicId;

    // 어떤 방의 메시지인지 찾기 위한 방 publicId
    @Field(type = FieldType.Keyword)
    private String roomPublicId;

    // 발신자 사용자 publicId
    @Field(type = FieldType.Keyword)
    private String senderUserPublicId;

    // 메시지 타입(TEXT, FILE, SYSTEM 등)
    @Field(type = FieldType.Keyword)
    private MessageType messageType;

    // 메시지 본문은 채팅 검색의 핵심 대상이라 부분검색용 ngram 필드를 같이 둔다.
    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(suffix = "ngram", type = FieldType.Text,
                            analyzer = "chat_ngram_analyzer",
                            searchAnalyzer = "chat_search_analyzer")
            }
    )
    private String messageBody;

    // 참조 도메인 정보
    @Field(type = FieldType.Keyword)
    private DomainType referenceType;

    @Field(type = FieldType.Keyword)
    private String referencePublicId;

    // 참조 코드/제목도 검색 후보가 될 수 있으므로 text로 둔다.
    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(suffix = "ngram", type = FieldType.Text,
                            analyzer = "chat_ngram_analyzer",
                            searchAnalyzer = "chat_search_analyzer")
            }
    )
    private String referenceCode;

    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(suffix = "ngram", type = FieldType.Text,
                            analyzer = "chat_ngram_analyzer",
                            searchAnalyzer = "chat_search_analyzer")
            }
    )
    private String referenceTitle;

    // 첨부파일 publicId 목록
    @Field(type = FieldType.Keyword)
    private List<String> attachmentPublicIds;

    // 삭제 상태 포함 여부를 구분하기 위한 soft delete 상태
    @Field(type = FieldType.Keyword)
    private Status status;

    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime createdAt;

    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime updatedAt;

    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime editedAt;

    public static ChatMessageDocument fromEntity(ChatMessage chatMessage) {
        return ChatMessageDocument.builder()
                .chatMessageId(chatMessage.getId())
                .publicId(chatMessage.getPublicId())
                .roomPublicId(chatMessage.getChatRoom().getPublicId())
                .senderUserPublicId(chatMessage.getSenderUserPublicId())
                .messageType(chatMessage.getMessageType())
                .messageBody(chatMessage.getMessageBody())
                .referenceType(chatMessage.getReferenceType())
                .referencePublicId(chatMessage.getReferencePublicId())
                .referenceCode(chatMessage.getReferenceCode())
                .referenceTitle(chatMessage.getReferenceTitle())
                .attachmentPublicIds(splitAttachmentIds(chatMessage.getAttachmentPublicIds()))
                .status(chatMessage.getStatus())
                .createdAt(chatMessage.getCreatedAt())
                .updatedAt(chatMessage.getUpdatedAt())
                .editedAt(chatMessage.getEditedAt())
                .build();
    }

    private static List<String> splitAttachmentIds(String attachmentPublicIds) {
        if (attachmentPublicIds == null || attachmentPublicIds.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(attachmentPublicIds.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
    }
}
