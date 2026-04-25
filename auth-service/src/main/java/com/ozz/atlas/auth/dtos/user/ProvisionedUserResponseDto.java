package com.ozz.atlas.auth.dtos.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 조직 초기 관리자 생성,
// 조직 사원 생성
// 둘 다 공통으로 쓰는 응답 DTO 입니다.
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "자동 생성된 사용자 계정 응답")
public class ProvisionedUserResponseDto {

    // 생성된 사용자 공개 ID
    @Schema(description = "생성된 사용자 공개 ID", example = "01KUSERABCDEFG1234567890ABC")
    private String userPublicId;

    // 소속 조직 공개 ID
    @Schema(description = "소속 조직 공개 ID", example = "01KORGABCDEFG1234567890ABC")
    private String organizationPublicId;

    // 서버가 자동 생성한 로그인 ID
    @Schema(description = "자동 생성된 로그인 ID", example = "user001@hanbit-aerospace")
    private String loginId;

    // 첫 로그인에 사용하는 임시 비밀번호
    @Schema(description = "임시 비밀번호", example = "Atlas!A1B2C3D4")
    private String temporaryPassword;

    // 첫 로그인 후 비밀번호 변경이 필요한지 여부
    @Schema(description = "비밀번호 변경 필요 여부", example = "true")
    private boolean passwordChangeRequired;
}
