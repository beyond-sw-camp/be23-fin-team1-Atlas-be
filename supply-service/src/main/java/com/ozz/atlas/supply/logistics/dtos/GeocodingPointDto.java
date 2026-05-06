package com.ozz.atlas.supply.logistics.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@Schema(description = "주소 지오코딩 좌표")
public class GeocodingPointDto {

    @Schema(description = "위도", example = "37.5000242")
    private BigDecimal latitude;
    @Schema(description = "경도", example = "127.0365086")
    private BigDecimal longitude;
}
