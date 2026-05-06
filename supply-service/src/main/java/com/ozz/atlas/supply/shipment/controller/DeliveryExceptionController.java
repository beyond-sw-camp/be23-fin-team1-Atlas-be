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
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/supply")
@Tag(name = "DeliveryException", description = "출하 배송 예외 등록 및 조회 API")
public class DeliveryExceptionController {

    private final DeliveryExceptionService deliveryExceptionService;

    public DeliveryExceptionController(DeliveryExceptionService deliveryExceptionService) {
        this.deliveryExceptionService = deliveryExceptionService;
    }

    @Operation(summary = "배송 예외 생성", description = "배송 중 출하에 발생한 예외 상황을 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "배송 예외 생성 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 오류"),
            @ApiResponse(responseCode = "403", description = "배송 예외 생성 권한 없음"),
            @ApiResponse(responseCode = "404", description = "출하를 찾을 수 없음")
    })
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

    @Operation(summary = "출하 배송 예외 목록 조회", description = "특정 출하에 등록된 배송 예외 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "배송 예외 목록 조회 성공"),
            @ApiResponse(responseCode = "404", description = "출하를 찾을 수 없음")
    })
    @GetMapping("/shipments/{publicId}/delivery-exceptions")
    public ResponseEntity<List<DeliveryExceptionResponseDto>> getDeliveryExceptions(
            @Parameter(description = "출하 공개 식별자", example = "01HZY1SHIPMENT123456789")
            @PathVariable String publicId
    ) {
        return ResponseEntity.ok(
                deliveryExceptionService.getDeliveryExceptionsByShipmentPublicId(publicId)
        );
    }
}
