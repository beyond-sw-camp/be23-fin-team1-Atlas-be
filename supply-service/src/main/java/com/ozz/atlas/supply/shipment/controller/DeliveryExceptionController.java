package com.ozz.atlas.supply.shipment.controller;

import com.ozz.atlas.supply.shipment.dtos.CreateDeliveryExceptionRequestDto;
import com.ozz.atlas.supply.shipment.dtos.DeliveryExceptionResponseDto;
import com.ozz.atlas.supply.shipment.service.DeliveryExceptionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/supply")
public class DeliveryExceptionController {

    private final DeliveryExceptionService deliveryExceptionService;

    public DeliveryExceptionController(DeliveryExceptionService deliveryExceptionService) {
        this.deliveryExceptionService = deliveryExceptionService;
    }

    @PostMapping("/delivery-exceptions")
    public ResponseEntity<DeliveryExceptionResponseDto> createDeliveryException(
            @Valid @RequestBody CreateDeliveryExceptionRequestDto dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(deliveryExceptionService.createDeliveryException(dto));
    }

    @GetMapping("/shipments/{publicId}/delivery-exceptions")
    public ResponseEntity<List<DeliveryExceptionResponseDto>> getDeliveryExceptions(
            @PathVariable String publicId
    ) {
        return ResponseEntity.ok(
                deliveryExceptionService.getDeliveryExceptionsByShipmentPublicId(publicId)
        );
    }
}
