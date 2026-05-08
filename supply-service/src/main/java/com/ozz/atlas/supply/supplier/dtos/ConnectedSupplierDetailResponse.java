package com.ozz.atlas.supply.supplier.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Connected Supplier Detail 값 응답")
public class ConnectedSupplierDetailResponse {

    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String publicId;
    @Schema(description = "조직 공개 식별자", example = "sample_public_id", nullable = true)
    private String organizationPublicId;
    @Schema(description = "코드", example = "CODE-001", nullable = true)
    private String supplierCode;
    @Schema(description = "이름", example = "샘플 이름", nullable = true)
    private String supplierName;
    @Schema(description = "상태", example = "ACTIVE", nullable = true)
    private SupplierStatus supplierStatus;
    @Schema(description = "이름", example = "샘플 이름", nullable = true)
    private String primaryContactName;
    @Schema(description = "이메일", example = "user@atlas.com", nullable = true)
    private String primaryContactEmail;
    @Schema(description = "연락처", example = "010-1234-5678", nullable = true)
    private String primaryContactPhone;
    @Schema(description = "생성 시각", example = "2026-05-08T10:00:00", nullable = true)
    private LocalDateTime createdAt;

    @Schema(description = "on Time Rate 값", example = "2026-05-08T10:00:00", nullable = true)
    private BigDecimal onTimeRate;
    @Schema(description = "개수", example = "1", nullable = true)
    private Long purchaseOrderCount;
    @Schema(description = "금액", example = "1", nullable = true)
    private BigDecimal cumulativeAmount;
    @Schema(description = "orders 값", nullable = true)
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
