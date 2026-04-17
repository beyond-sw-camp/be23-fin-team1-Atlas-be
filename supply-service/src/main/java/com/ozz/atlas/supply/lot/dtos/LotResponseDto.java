package com.ozz.atlas.supply.lot.dtos;

import com.ozz.atlas.supply.lot.domain.Lot;
import com.ozz.atlas.supply.lot.domain.LotStatus;
import com.ozz.atlas.supply.lot.domain.QualityStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class LotResponseDto {
    private String publicId;
    private String lotNumber;
    private String sourcePoItemPublicId;
    private String supplierPublicId;
    private String itemPublicId;
    private String supplierName;
    private String itemName;
    private LotStatus lotStatus;
    private LocalDateTime manufacturedAt;
    private LocalDateTime expiredAt;
    private BigDecimal qty;
    private String unit;
    private QualityStatus qualityStatus;
    private String currentNodePublicId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static LotResponseDto from(Lot entity, String supplierName, String itemName) {
        return LotResponseDto.builder()
                .publicId(entity.getPublicId())
                .lotNumber(entity.getLotNumber())
                .sourcePoItemPublicId(entity.getSourcePoItemPublicId())
                .supplierPublicId(entity.getSupplierPublicId())
                .itemPublicId(entity.getItemPublicId())
                .supplierName(supplierName)
                .itemName(itemName)
                .lotStatus(entity.getLotStatus())
                .manufacturedAt(entity.getManufacturedAt())
                .expiredAt(entity.getExpiredAt())
                .qty(entity.getQty())
                .unit(entity.getUnit())
                .qualityStatus(entity.getQualityStatus())
                .currentNodePublicId(entity.getCurrentNodePublicId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}