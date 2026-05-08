package com.ozz.atlas.supply.settlement.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Settlement 값 응답")
public class SettlementResponseDto {

    @Schema(description = "식별자", example = "1", nullable = true)
    private Long id;
    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String publicId;
    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String buyerOrganizationPublicId;
    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String supplierOrganizationPublicId;
    @Schema(description = "협력사 공개 식별자", example = "sample_public_id", nullable = true)
    private String supplierPublicId;
    @Schema(description = "유형", example = "DEFAULT", nullable = true)
    private SettlementTargetType targetType;
    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String targetPublicId;
    @Schema(description = "settlement Period Start 값", example = "sample", nullable = true)
    private LocalDate settlementPeriodStart;
    @Schema(description = "settlement Period End 값", example = "sample", nullable = true)
    private LocalDate settlementPeriodEnd;
    @Schema(description = "금액", example = "1", nullable = true)
    private BigDecimal amount;
    @Schema(description = "코드", example = "CODE-001", nullable = true)
    private SettlementCurrency currencyCode;
    @Schema(description = "상태", example = "ACTIVE", nullable = true)
    private SettlementStatus settlementStatus;
    @Schema(description = "settled At 값", example = "2026-05-08T10:00:00", nullable = true)
    private LocalDateTime settledAt;
    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String approvedByUserPublicId;
    @Schema(description = "cancelled At 값", example = "2026-05-08T10:00:00", nullable = true)
    private LocalDateTime cancelledAt;
    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String cancelledByUserPublicId;
    @Schema(description = "생성 시각", example = "2026-05-08T10:00:00", nullable = true)
    private LocalDateTime createdAt;
    @Schema(description = "수정 시각", example = "2026-05-08T10:00:00", nullable = true)
    private LocalDateTime updatedAt;
    @Schema(description = "details 값", nullable = true)
    private List<SettlementDetailResponseDto> details;
}
