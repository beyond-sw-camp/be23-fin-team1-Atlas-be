package com.ozz.atlas.control.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.ozz.atlas.common.domain.DomainType;
import com.ozz.atlas.control.chat.enums.MessageType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "메시지 모델")
public class ChatMessageDto {
    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String publicId;
    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String roomPublicId;
    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String senderUserPublicId;
    @Schema(description = "유형", example = "DEFAULT", nullable = true)
    private MessageType messageType;
    @Schema(description = "메시지", example = "샘플 내용", nullable = true)
    private String messageBody;
    @Schema(description = "유형", example = "DEFAULT", nullable = true)
    private DomainType referenceType;
    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String referencePublicId;
    @Schema(description = "코드", example = "CODE-001", nullable = true)
    private String referenceCode;
    @Schema(description = "제목", example = "샘플 이름", nullable = true)
    private String referenceTitle;
    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private List<String> attachmentPublicIds;
    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String parentMessagePublicId;
    @Schema(description = "메시지", example = "샘플 내용", nullable = true)
    private String parentMessageBody;
    @Schema(description = "이름", example = "샘플 이름", nullable = true)
    private String parentSenderDisplayName;
    @Schema(description = "sent At 값", example = "2026-05-08T10:00:00", nullable = true)
    private LocalDateTime sentAt;
    @Schema(description = "edited At 값", example = "2026-05-08T10:00:00", nullable = true)
    private LocalDateTime editedAt;
    @Schema(description = "is Deleted 값", example = "true", nullable = true)
    private boolean isDeleted;
    @Schema(description = "개수", example = "1", nullable = true)
    private int unreadCount;
}
