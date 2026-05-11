package com.ozz.atlas.supply.shipment.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ShipmentOrganizationNameLookupResponseDto {

    private String organizationPublicId;
    private String organizationName;
}
