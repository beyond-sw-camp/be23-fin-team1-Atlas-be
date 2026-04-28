package com.ozz.atlas.auth.dtos.user;

import com.ozz.atlas.auth.domain.User;
import com.ozz.atlas.auth.domain.UserRole;
import com.ozz.atlas.auth.search.document.UserDocument;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "사용자 목록 응답")
public class UserListDto {
    @Schema(description = "사용자 공개 식별자", example = "usr_01HZXA1B2C3D4E5F6G7H8J9K0")
    private String userPublicId;
    @Schema(description = "소속 조직 공개 식별자", example = "org_01HZX9X5D4P2Q7F8R9S1T2U3V4")
    private String organizationPublicId;
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
    @Schema(description = "사용자 권한", example = "USER")
    private UserRole userRole;
    @Schema(description = "프로필 첨부파일 공개 식별자", nullable = true)
    private String profileAttachmentPublicId;

    @Schema(description = "프로필 이미지 썸네일 경로", nullable = true)
    private String profileImageThumbPath;

    public static UserListDto fromEntity(User user){
        return UserListDto.builder()
                .userPublicId(user.getPublicId())
                .organizationPublicId(user.getOrganization().getPublicId())
                .loginId(user.getLoginId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .jobTitle(user.getJobTitle())
                .departmentPublicId(user.getDepartment() != null ? user.getDepartment().getPublicId() : null)
                .departmentCode(user.getDepartment() != null ? user.getDepartment().getDepartmentCode() : null)
                .departmentName(user.getDepartment() != null ? user.getDepartment().getDepartmentName() : null)
                .userRole(user.getUserRole())
                .firstName(user.getFirstName())
                .middleName(user.getMiddleName())
                .lastName(user.getLastName())
                .profileAttachmentPublicId(user.getProfileAttachmentPublicId())
                .profileImageThumbPath(user.getProfileImageThumbPath())
                .build();
    }

    public static UserListDto fromDocument(UserDocument user) {
        return UserListDto.builder()
                .userPublicId(user.getPublicId())
                .organizationPublicId(user.getOrganizationPublicId())
                .loginId(user.getLoginId())
                .email(user.getEmail())
                .userRole(user.getUserRole())
                .firstName(user.getFirstName())
                .middleName(user.getMiddleName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .jobTitle(user.getJobTitle())
                .departmentPublicId(user.getDepartmentPublicId())
                .departmentCode(user.getDepartmentCode())
                .departmentName(user.getDepartmentName())
                .profileAttachmentPublicId(user.getProfileAttachmentPublicId())
                .profileImageThumbPath(user.getProfileImageThumbPath())
                .build();
    }
}
