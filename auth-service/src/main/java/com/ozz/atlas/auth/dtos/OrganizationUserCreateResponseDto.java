package com.ozz.atlas.auth.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "조직 관리자의 직원 계정 생성 응답")
public class OrganizationUserCreateResponseDto {

    @Schema(description = "생성된 사용자 공개 ID", example = "01KUSERABCDEFG1234567890ABC")
    private String userPublicId;

    @Schema(description = "소속 조직 공개 ID", example = "01KORGABCDEFG1234567890ABC")
    private String organizationPublicId;

    @Schema(description = "자동 생성된 로그인 ID", example = "user001@hanbit-aerospace")
    private String loginId;

    @Schema(description = "최초 로그인에 사용하는 임시 비밀번호", example = "Atlas!A1B2C3D4")
    private String temporaryPassword;

    @Schema(description = "첫 로그인 후 비밀번호 변경 필요 여부", example = "true")
    private boolean passwordChangeRequired;
}
