package com.ozz.atlas.supply.logistics.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class GeocodingPointDto {

    private BigDecimal latitude;
    private BigDecimal longitude;
}
