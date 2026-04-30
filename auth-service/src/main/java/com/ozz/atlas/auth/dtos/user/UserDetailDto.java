package com.ozz.atlas.auth.dtos.user;

import com.ozz.atlas.auth.domain.User;
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
@Schema(description = "사용자 상세 응답")
public class UserDetailDto {
    @Schema(description = "사용자 공개 식별자", example = "usr_01HZXA1B2C3D4E5F6G7H8J9K0")
    private String userPublicId;
    @Schema(description = "소속 조직 공개 식별자", example = "org_01HZX9X5D4P2Q7F8R9S1T2U3V4")
    private String organizationPublicId;
    @Schema(description = "소속 조직명", example = "아틀라스 푸드 서플라이어")
    private String organizationName;
    @Schema(description = "소속 조직 영문명", example = "Atlas Foods Supplier")
    private String organizationEnglishName;
    @Schema(description = "사용자 내부 ID", example = "1")
    private Long userId;
    @Schema(description = "로그인 ID", example = "atlas_user01")
    private String loginId;
    @Schema(description = "이름", example = "철수")
    private String firstName;
    @Schema(description = "미들네임", example = "")
    private String middleName;
    @Schema(description = "성", example = "김")
    private String lastName;
    @Schema(description = "이메일", example = "user01@hanbit.com")
    private String email;
    @Schema(description = "전화번호", example = "010-5555-6666")
    private String phone;
    @Schema(description = "직책", example = "물류 운영 담당")
    private String jobTitle;
    @Schema(description = "부서 공개 식별자", example = "01KQ123456789ABCDEFGHJKMN", nullable = true)
    private String departmentPublicId;
    @Schema(description = "부서 코드", example = "LOGISTICS_DEPARTMENT", nullable = true)
    private String departmentCode;
    @Schema(description = "부서명", example = "물류 부서", nullable = true)
    private String departmentName;
    @Schema(description = "프로필 이미지 attachment 공개 식별자", example = "att_01HZXABCDEF1234567890", nullable = true)
    private String profileAttachmentPublicId;
    @Schema(description = "프로필 이미지 썸네일 경로", example = "https://atlas-media.s3.ap-northeast-2.amazonaws.com/thumbs/profile.png", nullable = true)
    private String profileImageThumbPath;
    @Schema(description = "사용자 권한", example = "USER")
    private UserRole userRole;


    public static UserDetailDto fromEntity(User user) {
        return UserDetailDto.builder()
                .userPublicId(user.getPublicId())
                .organizationPublicId(user.getOrganization().getPublicId())
                .organizationName(user.getOrganization().getOrganizationName())
                .organizationEnglishName(user.getOrganization().getOrganizationEnglishName())
                .userId(user.getUserId())
                .loginId(user.getLoginId())
                .firstName(user.getFirstName())
                .middleName(user.getMiddleName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .jobTitle(user.getJobTitle())
                .departmentPublicId(user.getDepartment() != null ? user.getDepartment().getPublicId() : null)
                .departmentCode(user.getDepartment() != null ? user.getDepartment().getDepartmentCode() : null)
                .departmentName(user.getDepartment() != null ? user.getDepartment().getDepartmentName() : null)
                .profileAttachmentPublicId(user.getProfileAttachmentPublicId())
                .profileImageThumbPath(user.getProfileImageThumbPath())
                .userRole(user.getUserRole())
                .build();
    }
}
