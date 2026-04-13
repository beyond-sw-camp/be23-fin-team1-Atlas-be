package com.ozz.atlas.control.chat.search.document;

import com.ozz.atlas.control.chat.domain.ChatMessage;
import com.ozz.atlas.control.chat.domain.ChatParticipant;
import com.ozz.atlas.control.chat.domain.ChatRoom;
import com.ozz.atlas.control.chat.enums.RoomStatus;
import com.ozz.atlas.common.jpa.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(indexName = "chat-rooms")
@Setting(settingPath = "/elasticsearch/chat-settings.json")
public class ChatRoomDocument {

    @Id
    private Long chatRoomId;

    // 외부에 노출하는 채팅방 publicId
    @Field(type = FieldType.Keyword)
    private String publicId;

    // 채팅방 이름은 방 이름 검색과 부분검색을 위해 ngram 필드를 함께 둠
    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(
                            suffix = "ngram",
                            type = FieldType.Text,
                            analyzer = "chat_ngram_analyzer",
                            searchAnalyzer = "chat_search_analyzer"
                    )
            }
    )
    private String roomName;

    // 방 상태는 OPEN / CLOSED 같은 정확 일치 필터용
    @Field(type = FieldType.Keyword)
    private RoomStatus roomStatus;

    // 방 생성자 사용자 publicId
    @Field(type = FieldType.Keyword)
    private String userAccountPublicId;

    // 마지막 메시지 publicId
    @Field(type = FieldType.Keyword)
    private String lastMessagePublicId;

    // 채팅방 목록에서 최근 메시지 검색도 가능하게 하기 위해 저장
    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(suffix = "ngram", type = FieldType.Text,
                            analyzer = "chat_ngram_analyzer",
                            searchAnalyzer = "chat_search_analyzer")
            }
    )
    private String lastMessageBody;

    // 참여자 publicId 목록
    // 현재 control-service에는 사용자 이름 정보가 없어서 우선 id 기준 검색만 지원
    @Field(type = FieldType.Keyword)
    private List<String> participantUserPublicIds;

    // 참여자 조직 publicId 목록
    @Field(type = FieldType.Keyword)
    private List<String> participantOrganizationPublicIds;

    // 생성/수정 시각은 LocalDateTime 문자열 포맷으로 저장
    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime createdAt;

    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime updatedAt;

    // 마지막 메시지 시각도 정렬/조회에 쓸 수 있게 저장
    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime lastMessageAt;

    public static ChatRoomDocument fromEntity(ChatRoom chatRoom,
                                              List<ChatParticipant> participants,
                                              ChatMessage lastMessage) {
        return ChatRoomDocument.builder()
                .chatRoomId(chatRoom.getId())
                .publicId(chatRoom.getPublicId())
                .roomName(chatRoom.getRoomName())
                .roomStatus(chatRoom.getRoomStatus())
                .userAccountPublicId(chatRoom.getUserAccountPublicId())
                .lastMessagePublicId(lastMessage != null ? lastMessage.getPublicId() : null)
                .lastMessageBody(resolveLastMessageBody(lastMessage))
                .participantUserPublicIds(
                        participants.stream()
                                .map(ChatParticipant::getUserPublicId)
                                .toList()
                )
                .participantOrganizationPublicIds(
                        participants.stream()
                                .map(ChatParticipant::getOrganizationPublicId)
                                .toList()
                )
                .createdAt(chatRoom.getCreatedAt())
                .updatedAt(chatRoom.getUpdatedAt())
                .lastMessageAt(lastMessage != null ? lastMessage.getCreatedAt() : null)
                .build();
    }
    // 마지막 메시지가 삭제된 상태면 원문 대신 안내 문구를 저장
    // 그래야 방 검색에서 삭제된 메시지 원문이 그대로 노출되지 않음
    private static String resolveLastMessageBody(ChatMessage lastMessage) {
        if (lastMessage == null) {
            return null;
        }

        if (lastMessage.getStatus() == Status.DELETE) {
            return "[삭제된 메시지입니다]";
        }

        return lastMessage.getMessageBody();
    }

}
