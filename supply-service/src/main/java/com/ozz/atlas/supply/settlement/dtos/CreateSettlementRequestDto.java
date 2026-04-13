package com.ozz.atlas.supply.settlement.dtos;

import com.ozz.atlas.supply.settlement.domain.SettlementCurrency;
import com.ozz.atlas.supply.settlement.domain.SettlementTargetType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class CreateSettlementRequestDto {

    @NotBlank
    private String supplierPublicId;

    @NotNull
    private SettlementTargetType targetType;

    @NotBlank
    private String targetPublicId;

    private LocalDate settlementPeriodStart;
    private LocalDate settlementPeriodEnd;

    @NotNull
    private SettlementCurrency currencyCode;

    @Valid
    @NotEmpty
    private List<CreateSettlementDetailRequestDto> details;
}
