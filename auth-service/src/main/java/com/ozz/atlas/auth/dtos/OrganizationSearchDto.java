package com.ozz.atlas.auth.dtos;

import com.ozz.atlas.auth.domain.OrganizationType;
import com.ozz.atlas.common.jpa.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "조직 검색 조건")
public class OrganizationSearchDto {
    @Schema(description = "조직 유형", example = "SUPPLIER", nullable = true)
    private OrganizationType organizationType;
    @Schema(description = "조직명", example = "Atlas", nullable = true)
    private String organizationName;
    @Schema(description = "조직 영문명", example = "Atlas Foods", nullable = true)
    private String organizationEnglishName;
    @Schema(description = "조직 코드", example = "ATLAS", nullable = true)
    private String organizationAlias;
    @Schema(description = "조직 상태", example = "ACTIVE", nullable = true)
    private Status status;
    @Schema(description = "통합 검색어", example = "atlas", nullable = true)
    private String keyword;

}
