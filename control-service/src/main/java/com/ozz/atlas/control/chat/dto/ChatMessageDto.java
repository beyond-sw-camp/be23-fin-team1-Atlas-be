package com.ozz.atlas.control.chat.dto;

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
public class ChatMessageDto {
    private String publicId;
    private String roomPublicId;
    private String senderUserPublicId;
    private MessageType messageType;
    private String messageBody;
    private DomainType referenceType;
    private String referencePublicId;
    private String referenceCode;
    private String referenceTitle;
    private List<String> attachmentPublicIds;
    private String parentMessagePublicId;
    private String parentMessageBody;
    private String parentSenderDisplayName;
    private LocalDateTime sentAt;
    private LocalDateTime editedAt;
    private boolean isDeleted;
    private int unreadCount;
}
