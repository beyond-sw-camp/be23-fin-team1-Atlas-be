package com.ozz.atlas.control.chat.search.document;

import com.ozz.atlas.control.chat.domain.ChatParticipant;
import com.ozz.atlas.control.client.dto.AuthUserDetailDto;
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

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(indexName = "chat-participants")
@Setting(settingPath = "/elasticsearch/chat-settings.json")
public class ChatParticipantDocument {

    @Id
    private Long chatParticipantId;

    // 어떤 채팅방의 참여자인지 구분하기 위한 채팅방 publicId
    @Field(type = FieldType.Keyword)
    private String roomPublicId;

    // 참여자 사용자 publicId
    // 다른 서비스와 연동할 때 기준 키로 사용
    @Field(type = FieldType.Keyword)
    private String userPublicId;

    // 참여자 소속 조직 publicId
    @Field(type = FieldType.Keyword)
    private String organizationPublicId;

    // 로그인 아이디는 정확 검색과 부분검색을 같이 지원
    // 예: supplier_manager_01, manager, supplier
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
    private String loginId;

    // 화면에서 사람이 읽는 이름
    // 성 + 이름 + 중간이름을 합쳐서 저장해두면
    // "홍길동", "길동", "홍" 같은 이름 검색에 활용
    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(
                            suffix = "ngram",
                            type = FieldType.Text,
                            analyzer = "chat_ngram_analyzer",
                            searchAnalyzer = "chat_search_analyzer"
                    )
            }
    )
    private String displayName;

    // 이메일도 사용자 검색에 자주 쓰이므로 부분검색 가능하게 저장
    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(
                            suffix = "ngram",
                            type = FieldType.Text,
                            analyzer = "chat_ngram_analyzer",
                            searchAnalyzer = "chat_search_analyzer"
                    )
            }
    )
    private String email;

    // OWNER, MEMBER 같은 참여자 역할
    @Field(type = FieldType.Keyword)
    private String participantRole;

    // 현재 활성 참여자인지 여부다.
    // 채팅방 나가기 후 비활성화된 사용자를 검색에서 제외할 때 사용
    @Field(type = FieldType.Boolean)
    private boolean activeYn;

    // 마지막으로 읽은 메시지 ID
    // 안읽음 계산이나 읽음 상태 추적에 활용
    @Field(type = FieldType.Long)
    private Long lastReadMessageId;

    // 프로필 이미지 썸네일 경로
    @Field(type = FieldType.Keyword)
    private String profileImageThumbPath;

    // 생성 시각
    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime createdAt;

    // 수정 시각
    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime updatedAt;

    // ChatParticipant 엔티티와 auth-service 에서 조회한 사용자 정보를 합쳐서
    // Elasticsearch 검색 문서를 생성
    public static ChatParticipantDocument fromEntity(ChatParticipant participant, AuthUserDetailDto user) {
        return ChatParticipantDocument.builder()
                .chatParticipantId(participant.getId())
                .roomPublicId(participant.getChatRoom().getPublicId())
                .userPublicId(participant.getUserPublicId())
                .organizationPublicId(participant.getOrganizationPublicId())
                .loginId(user != null ? user.getLoginId() : null)
                .displayName(buildDisplayName(user))
                .email(user != null ? user.getEmail() : null)
                .participantRole(participant.getParticipantRole())
                .activeYn(participant.isActiveYn())
                .lastReadMessageId(participant.getLastReadMessageId())
                .profileImageThumbPath(user != null ? user.getProfileImageThumbPath() : null)
                .createdAt(participant.getCreatedAt())
                .updatedAt(participant.getUpdatedAt())
                .build();
    }

    // auth-service 에서 받은 이름 정보를 한 줄 이름으로 합침
    // 예: lastName=홍, firstName=길동 이면 "홍길동"
    private static String buildDisplayName(AuthUserDetailDto user) {
        if (user == null) {
            return null;
        }

        String displayName = nullToEmpty(user.getLastName())
                + nullToEmpty(user.getFirstName())
                + nullToEmpty(user.getMiddleName());

        return displayName.isBlank() ? null : displayName.trim();
    }

    // null 문자열을 빈 문자열로 바꿔서 이름 조합 중 NPE 를 막음
    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
