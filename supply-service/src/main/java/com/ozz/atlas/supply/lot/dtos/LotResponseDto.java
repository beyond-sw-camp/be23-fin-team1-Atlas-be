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
    private Long id;
    private String publicId;
    private String lotNumber;
    private Long sourcePoItemId;
    private Long supplierId;
    private Long itemId;
    private LotStatus lotStatus;
    private LocalDateTime manufacturedAt;
    private LocalDateTime expiredAt;
    private BigDecimal qty;
    private String unit;
    private QualityStatus qualityStatus;
    private Long currentNodeId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static LotResponseDto from(Lot entity) {
        return LotResponseDto.builder()
                .id(entity.getId())
                .publicId(entity.getPublicId())
                .lotNumber(entity.getLotNumber())
                .sourcePoItemId(entity.getSourcePoItemId())
                .supplierId(entity.getSupplierId())
                .itemId(entity.getItemId())
                .lotStatus(entity.getLotStatus())
                .manufacturedAt(entity.getManufacturedAt())
                .expiredAt(entity.getExpiredAt())
                .qty(entity.getQty())
                .unit(entity.getUnit())
                .qualityStatus(entity.getQualityStatus())
                .currentNodeId(entity.getCurrentNodeId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}