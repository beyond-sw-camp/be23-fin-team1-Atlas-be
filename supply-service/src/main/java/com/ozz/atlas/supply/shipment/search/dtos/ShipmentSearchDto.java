package com.ozz.atlas.supply.shipment.search.dtos;

import com.ozz.atlas.supply.shipment.domain.ShipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShipmentSearchDto {

    private String keyword;

    private ShipmentStatus status; //배송상태

    private String originNodePublicId; //출발지

    private String destinationNodePublicId; //도착지

    private String currentNodePublicId; //현재위치

    private Boolean temperatureRequired; //온도관리 여부
}
