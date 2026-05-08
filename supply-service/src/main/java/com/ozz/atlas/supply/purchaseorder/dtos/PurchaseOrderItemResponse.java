package com.ozz.atlas.supply.purchaseorder.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import com.ozz.atlas.supply.logistics.domain.LogisticsNode;
import com.ozz.atlas.supply.purchaseorder.domain.PurchaseOrderItemStatus;
import com.ozz.atlas.supply.purchaseorder.domain.SupplyPurchaseOrderItem;
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
@Schema(description = "Purchase Order Item 값 응답")
public class PurchaseOrderItemResponse { // 발주 개별 아이템 정보용

    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String poItemPublicId;
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
    @Schema(description = "수량", example = "1", nullable = true)
    private Long orderedQty;
    @Schema(description = "수량", example = "1", nullable = true)
    private Long confirmedQty;
    @Schema(description = "가격", example = "1", nullable = true)
    private BigDecimal unitPrice;
    @Schema(description = "금액", example = "1", nullable = true)
    private BigDecimal lineAmount;
    @Schema(description = "상태", example = "ACTIVE", nullable = true)
    private PurchaseOrderItemStatus itemStatus;
    @Schema(description = "날짜", example = "2026-05-08", nullable = true)
    private LocalDate expectedDueDate;
    @Schema(description = "lead Time Days 값", example = "2026-05-08T10:00:00", nullable = true)
    private Integer leadTimeDays;
    @Schema(description = "partial Confirmation Allowed 값", example = "true", nullable = true)
    private Boolean partialConfirmationAllowed;
    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String arrivalLogisticsNodePublicId;
    @Schema(description = "이름", example = "샘플 이름", nullable = true)
    private String arrivalLogisticsNodeName;
    @Schema(description = "arrival Logistics Node Address 값", example = "sample", nullable = true)
    private String arrivalLogisticsNodeAddress;
    @Schema(description = "코드", example = "CODE-001", nullable = true)
    private String arrivalLogisticsNodeCode;
    @Schema(description = "수량", example = "1", nullable = true)
    private Long alreadyShippedQty;
    @Schema(description = "수량", example = "1", nullable = true)
    private Long remainingShippableQty;
    @Schema(description = "생성 시각", example = "2026-05-08T10:00:00", nullable = true)
    private LocalDateTime createdAt;
    @Schema(description = "수정 시각", example = "2026-05-08T10:00:00", nullable = true)
    private LocalDateTime updatedAt;
    public static PurchaseOrderItemResponse fromEntity(SupplyPurchaseOrderItem purchaseOrderItem) {
        LogisticsNode arrivalLogisticsNode = purchaseOrderItem.getArrivalLogisticsNode();
        return PurchaseOrderItemResponse.builder()
                .poItemPublicId(purchaseOrderItem.getPublicId())
                .itemPublicId(purchaseOrderItem.getItem().getPublicId())
                .itemCode(purchaseOrderItem.getItem().getItemCode())
                .itemName(purchaseOrderItem.getItem().getItemName())
                .primaryMediaFilePublicId(purchaseOrderItem.getItem().getPrimaryMediaFilePublicId())
                .unit(purchaseOrderItem.getItem().getUnit().name())
                .orderedQty(purchaseOrderItem.getOrderedQty())
                .confirmedQty(purchaseOrderItem.getConfirmedQty())
                .unitPrice(purchaseOrderItem.getUnitPrice())
                .lineAmount(purchaseOrderItem.getLineAmount())
                .itemStatus(purchaseOrderItem.getItemStatus())
                .expectedDueDate(purchaseOrderItem.getExpectedDueDate())
                .leadTimeDays(purchaseOrderItem.getLeadTimeDays())
                .partialConfirmationAllowed(purchaseOrderItem.getPartialConfirmationAllowed())
                .createdAt(purchaseOrderItem.getCreatedAt())
                .updatedAt(purchaseOrderItem.getUpdatedAt())
                .alreadyShippedQty(0L)
                .remainingShippableQty(purchaseOrderItem.getConfirmedQty())
                .arrivalLogisticsNodePublicId(arrivalLogisticsNode != null ? arrivalLogisticsNode.getPublicId() : null)
                .arrivalLogisticsNodeName(arrivalLogisticsNode  != null ? arrivalLogisticsNode.getNodeName() : null)
                .arrivalLogisticsNodeAddress(arrivalLogisticsNode  != null ? arrivalLogisticsNode.getAddress() : null)
                .arrivalLogisticsNodeCode(arrivalLogisticsNode != null ? arrivalLogisticsNode.getNodeCode() : null)
                .build();
    }
}
