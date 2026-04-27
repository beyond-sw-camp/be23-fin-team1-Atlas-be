package com.ozz.atlas.auth.dtos.organization;

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
public class OrganizationAliasLookupDto {

    private String organizationPublicId;
    private String organizationAlias;

    public static OrganizationAliasLookupDto fromEntity(Organization organization) {
        return OrganizationAliasLookupDto.builder()
                .organizationPublicId(organization.getPublicId())
                .organizationAlias(organization.getOrganizationAlias())
                .build();
    }
}
