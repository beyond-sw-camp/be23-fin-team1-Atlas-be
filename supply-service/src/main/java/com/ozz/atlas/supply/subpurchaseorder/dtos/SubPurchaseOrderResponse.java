package com.ozz.atlas.supply.subpurchaseorder.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import com.ozz.atlas.supply.subpurchaseorder.domain.SubPoStatus;
import com.ozz.atlas.supply.subpurchaseorder.domain.SupplySubPurchaseOrder;
import com.ozz.atlas.supply.supplier.domain.SupplierStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Sub Purchase Order 값 응답")
public class SubPurchaseOrderResponse {

    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String subPoPublicId;
    @Schema(description = "번호", example = "NO-2026-0001", nullable = true)
    private String subPoNumber;
    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String parentPoPublicId;
    @Schema(description = "번호", example = "NO-2026-0001", nullable = true)
    private String parentPoNumber;
    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String issuerSupplierPublicId;
    @Schema(description = "이름", example = "샘플 이름", nullable = true)
    private String issuerSupplierName;
    @Schema(description = "협력사 공개 식별자", example = "sample_public_id", nullable = true)
    private String supplierPublicId;
    @Schema(description = "코드", example = "CODE-001", nullable = true)
    private String supplierCode;
    @Schema(description = "이름", example = "샘플 이름", nullable = true)
    private String supplierName;
    @Schema(description = "금액", example = "1", nullable = true)
    private BigDecimal totalAmount;
    @Schema(description = "상태", example = "ACTIVE", nullable = true)
    private SubPoStatus subPoStatus;
    @Schema(description = "ordered At 값", example = "2026-05-08T10:00:00", nullable = true)
    private LocalDateTime orderedAt;
    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String createdByUserPublicId;
    @Schema(description = "항목 목록", nullable = true)
    private List<SubPurchaseOrderItemResponse> items;
    @Schema(description = "상태", example = "ACTIVE", nullable = true)
    private SupplierStatus supplierStatus;
    public static SubPurchaseOrderResponse fromEntity(SupplySubPurchaseOrder subPo, boolean includeItems) {
        return SubPurchaseOrderResponse.builder()
                .subPoPublicId(subPo.getPublicId())
                .subPoNumber(subPo.getSubPoNumber())
                .parentPoPublicId(subPo.getParentPurchaseOrder().getPublicId())
                .parentPoNumber(subPo.getParentPurchaseOrder().getPoNumber())
                .issuerSupplierPublicId(subPo.getParentPurchaseOrder().getSupplier().getPublicId())
                .issuerSupplierName(subPo.getParentPurchaseOrder().getSupplier().getSupplierName())
                .supplierPublicId(subPo.getSupplier().getPublicId())
                .supplierCode(subPo.getSupplier().getSupplierCode())
                .supplierName(subPo.getSupplier().getSupplierName())
                .totalAmount(subPo.getTotalAmount())
                .subPoStatus(subPo.getSubPoStatus())
                .orderedAt(subPo.getOrderedAt())
                .createdByUserPublicId(subPo.getCreatedByUserPublicId())
                .items(
                        includeItems
                                ? subPo.getActiveItems().stream()
                                .map(SubPurchaseOrderItemResponse::fromEntity)
                                .toList()
                                : null
                )
                .supplierStatus(subPo.getSupplier().getSupplierStatus())
                .build();
    }
}
