package com.ozz.atlas.control.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "이름 요청")
public class UpdateRoomNameRequestDto {
    @Schema(description = "이름", example = "샘플 이름", nullable = true)
    private String roomName;
}
