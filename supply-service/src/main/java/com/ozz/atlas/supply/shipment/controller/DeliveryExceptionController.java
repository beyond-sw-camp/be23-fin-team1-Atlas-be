package com.ozz.atlas.supply.shipment.controller;

import com.ozz.atlas.supply.shipment.dtos.CreateDeliveryExceptionRequestDto;
import com.ozz.atlas.supply.shipment.dtos.DeliveryExceptionResponseDto;
import com.ozz.atlas.supply.shipment.service.DeliveryExceptionService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/supply")
@Tag(name = "DeliveryException")
public class DeliveryExceptionController {

    private final DeliveryExceptionService deliveryExceptionService;

    public DeliveryExceptionController(DeliveryExceptionService deliveryExceptionService) {
        this.deliveryExceptionService = deliveryExceptionService;
    }

    @Operation(summary = "배송 예외 생성")
    @PostMapping("/delivery-exceptions")
    public ResponseEntity<DeliveryExceptionResponseDto> createDeliveryException(
            @Parameter(description = "요청 사용자 공개 식별자", example = "usr_01HZXA1B2C3D4E5F6G7H8J9K0")
            @RequestHeader(value = "X-User-Public-Id", required = false) String actorUserPublicId,
            @Parameter(description = "요청 조직 공개 식별자", example = "org_01HZX9X5D4P2Q7F8R9S1T2U3V4")
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @Valid @RequestBody CreateDeliveryExceptionRequestDto dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(deliveryExceptionService.createDeliveryException(dto, actorUserPublicId, organizationPublicId));
    }

    @Operation(summary = "출하 배송 예외 목록 조회")
    @GetMapping("/shipments/{publicId}/delivery-exceptions")
    public ResponseEntity<List<DeliveryExceptionResponseDto>> getDeliveryExceptions(
            @PathVariable String publicId
    ) {
        return ResponseEntity.ok(
                deliveryExceptionService.getDeliveryExceptionsByShipmentPublicId(publicId)
        );
    }
}
