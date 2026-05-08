package com.ozz.atlas.auth.dtos.organization;

import io.swagger.v3.oas.annotations.media.Schema;
import com.ozz.atlas.auth.domain.Organization;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
// supply-service에서 물류거점 코드 생성을 위해 조직 alias만 조회할 때 사용하는 최소 응답 DTO
@Schema(description = "Organization Alias Lookup 값 모델")
public class OrganizationAliasLookupDto {

    @Schema(description = "조직 공개 식별자", example = "sample_public_id", nullable = true)
    private String organizationPublicId;
    @Schema(description = "organization Alias 값", example = "sample", nullable = true)
    private String organizationAlias;
    public static OrganizationAliasLookupDto fromEntity(Organization organization) {
        return OrganizationAliasLookupDto.builder()
                .organizationPublicId(organization.getPublicId())
                .organizationAlias(organization.getOrganizationAlias())
                .build();
    }
}
