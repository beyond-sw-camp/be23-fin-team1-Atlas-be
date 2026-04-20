package com.ozz.atlas.supply.shipment.dtos;

import com.ozz.atlas.supply.shipment.domain.DeliveryExceptionSeverity;
import com.ozz.atlas.supply.shipment.domain.DeliveryExceptionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Schema(description = "배송 예외 생성 요청")
public class CreateDeliveryExceptionRequestDto {

    @NotBlank
    @Schema(description = "대상 출하 공개 식별자", example = "ship_01HZY1SHIPMENT123456789")
    private String shipmentPublicId;

    @NotNull
    @Schema(description = "배송 예외 유형", example = "DELAY")
    private DeliveryExceptionType exceptionType;

    @NotNull
    @Schema(description = "배송 예외 심각도", example = "HIGH")
    private DeliveryExceptionSeverity severity;

    @NotNull
    @Schema(description = "예외 감지 시각", example = "2026-04-20T21:30:00")
    private LocalDateTime detectedAt;

    @Schema(description = "예외 메모", example = "기상 악화로 허브 도착이 지연되었습니다.", nullable = true)
    private String note;
}
