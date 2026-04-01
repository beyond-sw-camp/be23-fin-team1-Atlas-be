package com.ozz.atlas.supply.item.dtos;

import com.ozz.atlas.supply.item.domain.SupplyItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemResponse {

    private Long id;
    private String publicId;
    private Long itemCategoryId;
    private String categoryName;
    private String itemCode;
    private String itemName;
    private String unit;
    private String spec;
    private Integer shelfLifeDays;
    private Integer activeYn;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ItemResponse from(SupplyItem item) {
        return ItemResponse.builder()
                .id(item.getId())
                .publicId(item.getPublicId())
                .itemCategoryId(item.getItemCategory().getId())
                .categoryName(item.getItemCategory().getCategoryName())
                .itemCode(item.getItemCode())
                .itemName(item.getItemName())
                .unit(item.getUnit())
                .spec(item.getSpec().name())
                .shelfLifeDays(item.getShelfLifeDays())
                .activeYn(item.getActiveYn())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
