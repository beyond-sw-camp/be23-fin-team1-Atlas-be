package com.ozz.atlas.supply.supplier.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectedSupplierSummaryResponse {

    private Long connectedSupplierCount;
    private BigDecimal averageOnTimeRate;
    private Integer averageLeadTimeDays;

    public static ConnectedSupplierSummaryResponse of(
            Long connectedSupplierCount,
            BigDecimal averageOnTimeRate,
            Integer averageLeadTimeDays
    ) {
        return ConnectedSupplierSummaryResponse.builder()
                .connectedSupplierCount(connectedSupplierCount != null ? connectedSupplierCount : 0L)
                .averageOnTimeRate(averageOnTimeRate)
                .averageLeadTimeDays(averageLeadTimeDays != null ? averageLeadTimeDays : 0)
                .build();
    }
}
