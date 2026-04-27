package com.ozz.atlas.auth.dtos.user;

import com.ozz.atlas.auth.domain.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "내 정보 요약 응답")
public class MyInfoDto {
    @Schema(description = "소속 조직 공개 식별자", example = "org_01HZX9X5D4P2Q7F8R9S1T2U3V4")
    private String organizationPublicId;

    @Schema(description = "사용자 공개 식별자", example = "usr_01HZXA1B2C3D4E5F6G7H8J9K0")
    private String userPublicId;

    @Schema(description = "사용자 역할", example = "ADMIN")
    private UserRole role;

    @Schema(description = "프로필 이미지 attachment 공개 식별자", example = "att_01HZXABCDEF1234567890", nullable = true)
    private String profileAttachmentPublicId;

    @Schema(description = "프로필 이미지 썸네일 경로", example = "https://atlas-media.s3.ap-northeast-2.amazonaws.com/thumbs/profile.png", nullable = true)
    private String profileImageThumbPath;
}
