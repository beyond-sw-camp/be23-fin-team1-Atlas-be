package com.ozz.atlas.supply.shipment.dtos;

import com.ozz.atlas.supply.shipment.domain.ShipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ShipmentListResponseDto {

    private String publicId;
    private String shipmentNumber;
    private String purchaseOrderPublicId;
    private String subPurchaseOrderPublicId;
    private String carrierName;
    private String destinationNodePublicId;
    private String currentNodePublicId;
    private LocalDateTime arrivalEta;
    private ShipmentStatus status;

}
