package com.ozz.atlas.supply.item.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import com.ozz.atlas.common.jpa.Status;
import com.ozz.atlas.supply.item.domain.SupplyItem;
import com.ozz.atlas.supply.item.domain.SupplyType;
import com.ozz.atlas.supply.supplier.capability.domain.SupplySupplierItemCapability;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Item 값 응답")
public class ItemResponse {

    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String publicId;
    @Schema(description = "협력사 공개 식별자", example = "sample_public_id", nullable = true)
    private String supplierPublicId;
    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String supplierOrganizationPublicId;
    @Schema(description = "이름", example = "샘플 이름", nullable = true)
    private String supplierName;
    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String itemCategoryPublicId;
    @Schema(description = "이름", example = "샘플 이름", nullable = true)
    private String categoryName;
    @Schema(description = "유형", example = "DEFAULT", nullable = true)
    private SupplyType supplyType;
    @Schema(description = "코드", example = "CODE-001", nullable = true)
    private String itemCode;
    @Schema(description = "이름", example = "샘플 이름", nullable = true)
    private String itemName;
    @Schema(description = "unit 값", example = "sample", nullable = true)
    private String unit;
    @Schema(description = "가격", example = "1", nullable = true)
    private BigDecimal unitPrice;
    @Schema(description = "spec 값", example = "sample", nullable = true)
    private String spec;
    @Schema(description = "shelf Life Days 값", example = "1", nullable = true)
    private Integer shelfLifeDays;
    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String primaryMediaFilePublicId;
    @Schema(description = "상태", example = "ACTIVE", nullable = true)
    private Status status;
    @Schema(description = "생성 시각", example = "2026-05-08T10:00:00", nullable = true)
    private LocalDateTime createdAt;
    @Schema(description = "수정 시각", example = "2026-05-08T10:00:00", nullable = true)
    private LocalDateTime updatedAt;
    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String originLogisticsNodePublicId;
    @Schema(description = "이름", example = "샘플 이름", nullable = true)
    private String originLogisticsNodeName;
    @Schema(description = "lead Time Days 값", example = "2026-05-08T10:00:00", nullable = true)
    private Integer leadTimeDays;
    @Schema(description = "monthly Capacity 값", example = "1", nullable = true)
    private Long monthlyCapacity;
    @Schema(description = "수량", example = "1", nullable = true)
    private Long availableQty;
    @Schema(description = "moq 값", example = "1", nullable = true)
    private Long moq;
    @Schema(description = "partial Confirmation Allowed 값", example = "true", nullable = true)
    private Boolean partialConfirmationAllowed;

    public static ItemResponse fromEntity(SupplyItem item) {
        return ItemResponse.builder()
                .publicId(item.getPublicId())
                .supplierPublicId(item.getSupplier().getPublicId())
                .supplierOrganizationPublicId(item.getSupplier().getOrganizationPublicId())
                .supplierName(item.getSupplier().getSupplierName())
                .itemCategoryPublicId(item.getItemCategory().getPublicId())
                .categoryName(item.getItemCategory().getCategoryName())
                .supplyType(item.getSupplyType())
                .itemCode(item.getItemCode())
                .itemName(item.getItemName())
                .unit(item.getUnit().name())
                .unitPrice(item.getUnitPrice())
                .spec(item.getSpec())
                .shelfLifeDays(item.getShelfLifeDays())
                .primaryMediaFilePublicId(item.getPrimaryMediaFilePublicId())
                .status(item.getStatus())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .originLogisticsNodePublicId(
                        item.getOriginLogisticsNode() != null ? item.getOriginLogisticsNode().getPublicId() : null
                )
                .originLogisticsNodeName(
                        item.getOriginLogisticsNode() != null ? item.getOriginLogisticsNode().getNodeName() : null
                )
                .build();
    }
    public static ItemResponse fromEntityWithCapability(
            SupplyItem item,
            SupplySupplierItemCapability capability
    ) {
        ItemResponse response = fromEntity(item);

        response.leadTimeDays = capability != null ? capability.getLeadTimeDays() : null;
        response.monthlyCapacity = capability != null ? capability.getMonthlyCapacity() : null;
        response.availableQty = capability != null ? capability.getAvailableQty() : null;
        response.moq = capability != null ? capability.getMoq() : null;
        response.partialConfirmationAllowed =
                capability != null ? capability.getPartialConfirmationAllowed() : null;

        return response;
    }
}
