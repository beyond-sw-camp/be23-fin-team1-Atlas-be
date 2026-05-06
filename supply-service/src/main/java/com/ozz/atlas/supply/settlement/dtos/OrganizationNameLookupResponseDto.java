package com.ozz.atlas.supply.settlement.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationNameLookupResponseDto {

    private String organizationPublicId;

    private String organizationName;

    private String contactFirstName;

    private String contactMiddleName;

    private String contactLastName;

    private String contactPhone;


}
