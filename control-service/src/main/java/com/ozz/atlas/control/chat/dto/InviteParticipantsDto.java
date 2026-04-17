package com.ozz.atlas.control.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "채팅방 참여자 초대 요청")
public class InviteParticipantsDto {
    @Schema(description = "초대 요청자 사용자 공개 식별자", example = "usr_01HZXA1B2C3D4E5F6G7H8J9K0")
    private String inviterPublicId;

    @Schema(description = "초대 대상 사용자 공개 식별자 목록", example = "[\"usr_01HZY4U2\", \"usr_01HZY4U3\"]")
    private List<String> targetUserPublicIds;
}
