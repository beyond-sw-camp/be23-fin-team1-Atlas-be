package com.ozz.atlas.supply.item.dtos;

import com.ozz.atlas.common.jpa.Status;
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
    private Long itemCategoryId;
    private String categoryName;
    private String itemCode;
    private String itemName;
    private String unit;
    private String spec;
    private Integer shelfLifeDays;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ItemResponse fromEntity(SupplyItem item) {
        return ItemResponse.builder()
                .id(item.getId())
                .itemCategoryId(item.getItemCategory().getId())
                .categoryName(item.getItemCategory().getCategoryName())
                .itemCode(item.getItemCode())
                .itemName(item.getItemName())
                .unit(item.getUnit())
                .spec(item.getSpec().name())
                .shelfLifeDays(item.getShelfLifeDays())
                .status(item.getStatus())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
