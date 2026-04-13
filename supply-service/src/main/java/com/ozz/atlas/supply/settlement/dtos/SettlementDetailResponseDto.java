package com.ozz.atlas.supply.settlement.dtos;

import com.ozz.atlas.supply.settlement.domain.SettlementDetailStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class SettlementDetailResponseDto {

    private String publicId;
    private Long poItemId;
    private Long itemId;
    private BigDecimal qty;
    private BigDecimal unitPrice;
    private BigDecimal amount;
    private SettlementDetailStatus detailStatus;
}
