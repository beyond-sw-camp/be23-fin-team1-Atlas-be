package com.ozz.atlas.supply.supplier.certificate.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationNameLookupResponseDto {

    private String organizationPublicId;
    private String organizationName;
}
