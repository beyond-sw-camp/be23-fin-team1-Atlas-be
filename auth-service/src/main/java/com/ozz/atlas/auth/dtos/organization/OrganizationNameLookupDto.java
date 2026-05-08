package com.ozz.atlas.auth.dtos.organization;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "이름 모델")
public class OrganizationNameLookupDto {

    @Schema(description = "조직 공개 식별자", example = "sample_public_id", nullable = true)
    private String organizationPublicId;

    @Schema(description = "이름", example = "샘플 이름", nullable = true)
    private String organizationName;

    @Schema(description = "이름", example = "샘플 이름", nullable = true)
    private String contactFirstName;

    @Schema(description = "식별자", example = "샘플 이름", nullable = true)
    private String contactMiddleName;

    @Schema(description = "이름", example = "샘플 이름", nullable = true)
    private String contactLastName;

    @Schema(description = "연락처", example = "010-1234-5678", nullable = true)
    private String contactPhone;

}
