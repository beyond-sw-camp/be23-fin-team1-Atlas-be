package com.ozz.atlas.auth.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "조직 최초 ORG_ADMIN 생성 요청")
public class InitialOrgAdminCreateDto {

    @NotBlank(message = "로그인 ID는 비어 있을 수 없습니다.")
    @Schema(description = "최초 대표자 로그인 ID", example = "hanbit_admin")
    private String loginId;

    @NotBlank(message = "이름은 비어 있을 수 없습니다.")
    @Schema(description = "이름", example = "준호")
    private String firstName;

    @Schema(description = "미들네임", example = "")
    private String middleName;

    @NotBlank(message = "성은 비어 있을 수 없습니다.")
    @Schema(description = "성", example = "이")
    private String lastName;

    @NotBlank(message = "이메일은 비어 있을 수 없습니다.")
    @Schema(description = "이메일", example = "admin@hanbit.com")
    private String email;

    @NotBlank(message = "연락처는 비어 있을 수 없습니다.")
    @Schema(description = "전화번호", example = "010-2345-6789")
    private String phone;

    @Schema(description = "직책", example = "대표자")
    private String jobTitle;
}
