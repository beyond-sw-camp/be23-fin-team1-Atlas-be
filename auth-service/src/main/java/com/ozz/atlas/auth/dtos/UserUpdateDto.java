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
@Schema(description = "사용자 정보 수정 요청")
public class UserUpdateDto {
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
    @Schema(description = "부서 공개 식별자", example = "01KQ123456789ABCDEFGHJKMN")
    private String departmentPublicId;
    @Schema(description = "프로필 이미지 attachment 공개 식별자", example = "att_01HZXABCDEF1234567890")
    private String profileAttachmentPublicId;
    @Schema(description = "프로필 이미지 썸네일 경로", example = "https://atlas-media.s3.ap-northeast-2.amazonaws.com/thumbs/profile.png")
    private String profileImageThumbPath;

}
