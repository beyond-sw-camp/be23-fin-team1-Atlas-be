package com.ozz.atlas.control.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "메시지 요청")
public class UpdateMessageRequestDto {
    @Schema(description = "메시지", example = "샘플 내용", nullable = true)
    private String messageBody;
}