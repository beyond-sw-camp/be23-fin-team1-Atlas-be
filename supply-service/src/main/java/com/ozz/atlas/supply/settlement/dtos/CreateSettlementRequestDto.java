package com.ozz.atlas.supply.settlement.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import com.ozz.atlas.supply.settlement.domain.SettlementCurrency;
import com.ozz.atlas.supply.settlement.domain.SettlementTargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@Schema(description = "Create Settlement 값 요청")
public class CreateSettlementRequestDto {

    @NotNull
    @Schema(description = "유형", example = "DEFAULT")
    private SettlementTargetType targetType;

    @NotBlank
    @Schema(description = "공개 식별자", example = "sample_public_id")
    private String targetPublicId;

    @Schema(description = "settlement Period Start 값", example = "sample", nullable = true)
    private LocalDate settlementPeriodStart;
    @Schema(description = "settlement Period End 값", example = "sample", nullable = true)
    private LocalDate settlementPeriodEnd;

    @NotNull
    @Schema(description = "코드", example = "CODE-001")
    private SettlementCurrency currencyCode;
}
