package com.ozz.atlas.control.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "채팅방 참여자 정보")
public class ChatParticipantDto {
    @Schema(description = "사용자 공개 식별자", example = "usr_01HZXA1B2C3D4E5F6G7H8J9K0")
    private String userPublicId;
    @Schema(description = "표시 이름", example = "홍길동")
    private String displayName;
    @Schema(description = "프로필 이미지 썸네일 경로", example = "http://...", nullable = true)
    private String profileImageThumbPath;
}
