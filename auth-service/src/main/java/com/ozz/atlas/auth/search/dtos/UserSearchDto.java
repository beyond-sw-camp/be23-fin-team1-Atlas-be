package com.ozz.atlas.auth.search.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import com.ozz.atlas.auth.domain.UserRole;
import com.ozz.atlas.common.jpa.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "User 값 검색 조건")
public class UserSearchDto {
    @Schema(description = "조직 공개 식별자", example = "sample_public_id", nullable = true)
    private String organizationPublicId;
    @Schema(description = "user Role 값", example = "sample", nullable = true)
    private UserRole userRole;
    @Schema(description = "식별자", example = "1", nullable = true)
    private String loginId;
    @Schema(description = "이름", example = "샘플 이름", nullable = true)
    private String firstName;
    @Schema(description = "식별자", example = "샘플 이름", nullable = true)
    private String middleName;
    @Schema(description = "이름", example = "샘플 이름", nullable = true)
    private String lastName;
    @Schema(description = "상태", example = "ACTIVE", nullable = true)
    private Status status;
    @Schema(description = "검색어", example = "검색어", nullable = true)
    private String keyword;
}
