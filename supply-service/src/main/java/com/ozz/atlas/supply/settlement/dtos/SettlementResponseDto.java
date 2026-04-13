package com.ozz.atlas.supply.settlement.dtos;

import com.ozz.atlas.supply.settlement.domain.SettlementCurrency;
import com.ozz.atlas.supply.settlement.domain.SettlementStatus;
import com.ozz.atlas.supply.settlement.domain.SettlementTargetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class SettlementResponseDto {

    private Long id;
    private String supplierPublicId;
    private SettlementTargetType targetType;
    private String targetPublicId;
    private LocalDate settlementPeriodStart;
    private LocalDate settlementPeriodEnd;
    private BigDecimal amount;
    private SettlementCurrency currencyCode;
    private SettlementStatus settlementStatus;
    private LocalDateTime settledAt;
    private String approvedByUserPublicId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<SettlementDetailResponseDto> details;
}
