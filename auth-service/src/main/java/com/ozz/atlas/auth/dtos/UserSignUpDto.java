package com.ozz.atlas.auth.dtos;

import com.ozz.atlas.auth.domain.Organization;
import com.ozz.atlas.auth.domain.Department;
import com.ozz.atlas.auth.domain.User;
import com.ozz.atlas.auth.domain.UserRole;
import com.ozz.atlas.common.jpa.Status;
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
@Schema(description = "사용자 회원가입 요청")
public class UserSignUpDto {

    @NotBlank(message = "조직 식별값은 비어있으면 안 됩니다.")
    @Schema(description = "소속 조직 공개 식별자", example = "org_01HZX9X5D4P2Q7F8R9S1T2U3V4")
    private String organizationPublicId;

    @NotBlank(message = "ID가 비어있으면 안 됩니다.")
    @Schema(description = "로그인 ID", example = "atlas_user01")
    private String loginId;

    @NotBlank(message = "비밀번호가 비어있으면 안 됩니다.")
    @Schema(description = "초기 비밀번호", example = "Atlas!234")
    private String password;

    @NotBlank(message = "이름이 비어있으면 안 됩니다.")
    @Schema(description = "이름", example = "Seoyeon")
    private String firstName;
    @Schema(description = "미들네임", example = "")
    private String middleName;
    @NotBlank(message = "성이 비어있으면 안 됩니다.")
    @Schema(description = "성", example = "Lee")
    private String lastName;

    @NotBlank(message = "email이 비어있으면 안 됩니다.")
    @Schema(description = "이메일", example = "seoyeon.lee@atlas.com")
    private String email;

    @NotBlank(message = "연락처가 비어있으면 안 됩니다.")
    @Schema(description = "전화번호", example = "010-8888-9999")
    private String phone;

    @Schema(description = "직책", example = "Procurement Manager")
    private String jobTitle;

    @Schema(description = "부서 공개 식별자", example = "01KQ123456789ABCDEFGHJKMN", nullable = true)
    private String departmentPublicId;

    @Schema(description = "프로필 이미지 파일 공개 식별자", example = "file_01HZXABCDEF1234567890", nullable = true)
    private String profileImagePublicId;

    @Schema(description = "사용자 상태", example = "ACTIVE", nullable = true)
    private Status status;

    public User toEntity(Organization organization, Department department, String encodedPassword) {
        return User.builder()
                .organization(organization)
                .department(department)
                .loginId(this.loginId)
                .password(encodedPassword)
                .firstName(this.firstName)
                .middleName(this.middleName)
                .lastName(this.lastName)
                .email(this.email)
                .phone(this.phone)
                .jobTitle(this.jobTitle)
                .userRole(UserRole.USER)
                .status(Status.ACTIVE)
                .build();
    }



}
