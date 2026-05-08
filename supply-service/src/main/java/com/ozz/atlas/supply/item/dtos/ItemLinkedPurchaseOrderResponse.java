package com.ozz.atlas.supply.item.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import com.ozz.atlas.supply.purchaseorder.domain.PoStatus;
import com.ozz.atlas.supply.purchaseorder.domain.PurchaseOrderItemStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Item Linked Purchase Order 값 응답")
public class ItemLinkedPurchaseOrderResponse {
    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String poPublicId;
    @Schema(description = "번호", example = "NO-2026-0001", nullable = true)
    private String poNumber;
    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String buyerOrganizationPublicId;
    @Schema(description = "상태", example = "ACTIVE", nullable = true)
    private PoStatus poStatus;
    @Schema(description = "ordered At 값", example = "2026-05-08T10:00:00", nullable = true)
    private LocalDateTime orderedAt;
    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String poItemPublicId;
    @Schema(description = "수량", example = "1", nullable = true)
    private Long orderedQty;
    @Schema(description = "수량", example = "1", nullable = true)
    private Long confirmedQty;
    @Schema(description = "상태", example = "ACTIVE", nullable = true)
    private PurchaseOrderItemStatus itemStatus;
    @Schema(description = "날짜", example = "2026-05-08", nullable = true)
    private LocalDate expectedDueDate;
}
