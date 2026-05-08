package com.ozz.atlas.supply.subpurchaseorder.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import com.ozz.atlas.supply.subpurchaseorder.domain.SubPurchaseOrderLineStatus;
import com.ozz.atlas.supply.subpurchaseorder.domain.SupplySubPurchaseOrderItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Sub Purchase Order Item 값 응답")
public class SubPurchaseOrderItemResponse {

    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String parentPoItemPublicId;
    @Schema(description = "품목 공개 식별자", example = "sample_public_id", nullable = true)
    private String itemPublicId;
    @Schema(description = "코드", example = "CODE-001", nullable = true)
    private String itemCode;
    @Schema(description = "이름", example = "샘플 이름", nullable = true)
    private String itemName;
    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String primaryMediaFilePublicId;
    @Schema(description = "unit 값", example = "sample", nullable = true)
    private String unit;
    @Schema(description = "가격", example = "1", nullable = true)
    private BigDecimal unitPrice;
    @Schema(description = "금액", example = "1", nullable = true)
    private BigDecimal lineAmount;
    @Schema(description = "수량", example = "1", nullable = true)
    private Long orderedQty;
    @Schema(description = "수량", example = "1", nullable = true)
    private Long confirmedQty;
    @Schema(description = "상태", example = "ACTIVE", nullable = true)
    private SubPurchaseOrderLineStatus lineStatus;
    @Schema(description = "날짜", example = "2026-05-08", nullable = true)
    private LocalDate expectedDueDate;
    @Schema(description = "lead Time Days 값", example = "2026-05-08T10:00:00", nullable = true)
    private Integer leadTimeDays;
    @Schema(description = "partial Confirmation Allowed 값", example = "true", nullable = true)
    private Boolean partialConfirmationAllowed;
    @Schema(description = "생성 시각", example = "2026-05-08T10:00:00", nullable = true)
    private LocalDateTime createdAt;
    @Schema(description = "수정 시각", example = "2026-05-08T10:00:00", nullable = true)
    private LocalDateTime updatedAt;
    public static SubPurchaseOrderItemResponse fromEntity(SupplySubPurchaseOrderItem item) {
        return SubPurchaseOrderItemResponse.builder()
                .parentPoItemPublicId(item.getParentPurchaseOrderItem().getPublicId())
                .itemPublicId(item.getItem().getPublicId())
                .itemCode(item.getItem().getItemCode())
                .itemName(item.getItem().getItemName())
                .primaryMediaFilePublicId(item.getItem().getPrimaryMediaFilePublicId())
                .unit(item.getItem().getUnit().name())
                .unitPrice(item.getUnitPrice())
                .lineAmount(item.getLineAmount())
                .orderedQty(item.getOrderedQty())
                .confirmedQty(item.getConfirmedQty())
                .lineStatus(item.getLineStatus())
                .expectedDueDate(item.getExpectedDueDate())
                .leadTimeDays(item.getLeadTimeDays())
                .partialConfirmationAllowed(item.getPartialConfirmationAllowed())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

}
