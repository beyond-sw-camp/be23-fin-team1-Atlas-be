package com.ozz.atlas.supply.logistics.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationAliasLookupResponseDto {

    private String organizationPublicId;
    private String organizationAlias;
}
