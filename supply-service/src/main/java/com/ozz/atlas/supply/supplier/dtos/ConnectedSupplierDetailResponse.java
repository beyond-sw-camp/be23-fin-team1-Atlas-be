package com.ozz.atlas.supply.supplier.dtos;

import com.ozz.atlas.supply.supplier.domain.SupplierStatus;
import com.ozz.atlas.supply.supplier.domain.SupplySupplier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectedSupplierDetailResponse {

    private String publicId;
    private String organizationPublicId;
    private String supplierCode;
    private String supplierName;
    private SupplierStatus supplierStatus;
    private String primaryContactName;
    private String primaryContactEmail;
    private String primaryContactPhone;
    private LocalDateTime createdAt;

    private BigDecimal onTimeRate;
    private Long purchaseOrderCount;
    private BigDecimal cumulativeAmount;
    private List<ConnectedSupplierOrderResponse> orders;

    public static ConnectedSupplierDetailResponse of(
            SupplySupplier supplier,
            BigDecimal onTimeRate,
            Long purchaseOrderCount,
            BigDecimal cumulativeAmount,
            List<ConnectedSupplierOrderResponse> orders
    ) {
        return ConnectedSupplierDetailResponse.builder()
                .publicId(supplier.getPublicId())
                .organizationPublicId(supplier.getOrganizationPublicId())
                .supplierCode(supplier.getSupplierCode())
                .supplierName(supplier.getSupplierName())
                .supplierStatus(supplier.getSupplierStatus())
                .primaryContactName(supplier.getPrimaryContactName())
                .primaryContactEmail(supplier.getPrimaryContactEmail())
                .primaryContactPhone(supplier.getPrimaryContactPhone())
                .createdAt(supplier.getCreatedAt())
                .onTimeRate(onTimeRate)
                .purchaseOrderCount(purchaseOrderCount != null ? purchaseOrderCount : 0L)
                .cumulativeAmount(cumulativeAmount != null ? cumulativeAmount : BigDecimal.ZERO)
                .orders(orders != null ? orders : List.of())
                .build();
    }
}
