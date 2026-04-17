package com.ozz.atlas.control.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "채팅 읽음 처리 요청")
public class MarkAsReadRequestDto {
    @Schema(description = "마지막으로 읽은 메시지 공개 식별자", example = "msg_01HZY4MSG123456789", nullable = true)
    private String lastReadMessagePublicId;
}
