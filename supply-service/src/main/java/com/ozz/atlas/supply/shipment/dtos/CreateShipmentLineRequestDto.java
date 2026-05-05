package com.ozz.atlas.supply.shipment.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Schema(description = "출하 생성 품목 요청")
public class CreateShipmentLineRequestDto {

    @NotBlank
    @Schema(description = "발주 품목 공개 식별자", example = "01HZY1POITEM12345678901234")
    private String sourceItemPublicId;

    @NotNull
    @Min(1)
    @Schema(description = "출하 수량", example = "100")
    private Long quantity;
}
