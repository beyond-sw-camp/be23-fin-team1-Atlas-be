package com.ozz.atlas.supply.item.dtos;

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
public class ItemResponse {

    private String publicId;
    private String supplierPublicId;
    private String supplierOrganizationPublicId;
    private String supplierName;
    private String itemCategoryPublicId;
    private String categoryName;
    private SupplyType supplyType;
    private String itemCode;
    private String itemName;
    private String unit;
    private BigDecimal unitPrice;
    private String spec;
    private Integer shelfLifeDays;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String originLogisticsNodePublicId;
    private String originLogisticsNodeName;
    private Integer leadTimeDays;
    private Long monthlyCapacity;
    private Long availableQty;
    private Long moq;
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
