package com.ozz.atlas.control.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Auth User Detail 값 모델")
public class AuthUserDetailDto {

    @Schema(description = "사용자 공개 식별자", example = "sample_public_id", nullable = true)
    private String userPublicId;

    @Schema(description = "조직 공개 식별자", example = "sample_public_id", nullable = true)
    private String organizationPublicId;

    @Schema(description = "식별자", example = "1", nullable = true)
    private Long userId;

    @Schema(description = "식별자", example = "1", nullable = true)
    private String loginId;

    @Schema(description = "이름", example = "샘플 이름", nullable = true)
    private String firstName;

    @Schema(description = "식별자", example = "샘플 이름", nullable = true)
    private String middleName;

    @Schema(description = "이름", example = "샘플 이름", nullable = true)
    private String lastName;

    @Schema(description = "이메일", example = "user@atlas.com", nullable = true)
    private String email;

    @Schema(description = "연락처", example = "010-1234-5678", nullable = true)
    private String phone;

    @Schema(description = "제목", example = "샘플 이름", nullable = true)
    private String jobTitle;

    @Schema(description = "profile Image Thumb Path 값", example = "sample", nullable = true)
    private String profileImageThumbPath;
}
