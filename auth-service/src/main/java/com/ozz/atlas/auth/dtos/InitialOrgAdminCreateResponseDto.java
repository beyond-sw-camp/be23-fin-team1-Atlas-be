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
@Schema(description = "조직 최초 ORG_ADMIN 생성 응답")
public class InitialOrgAdminCreateResponseDto {

    @Schema(description = "생성된 사용자 공개 ID", example = "01KABCDEFG1234567890ABCDEFG")
    private String userPublicId;

    @Schema(description = "대상 조직 공개 ID", example = "01KORGABCDEFG1234567890ABCD")
    private String organizationPublicId;

    // 서버가 자동으로 만든 로그인 ID입니다.
    @Schema(description = "자동 생성된 로그인 ID", example = "user001@hanwha-aerospace")
    private String loginId;

    @Schema(description = "최초 로그인에 사용하는 임시 비밀번호", example = "Atlas!A1B2C3D4")
    private String temporaryPassword;

    @Schema(description = "첫 로그인 후 비밀번호 변경 필요 여부", example = "true")
    private boolean passwordChangeRequired;
}
