package com.ozz.atlas.supply.item.dtos;

import com.ozz.atlas.supply.item.domain.SupplyItemCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemCategoryResponse {
    private Long id;
    private Long parentCategoryId;
    private String categoryName;
    private Integer categoryLevel;
    private Integer sortOrder;
    private Integer activeYn;
    private LocalDateTime createdAt;

    public static ItemCategoryResponse from(SupplyItemCategory category) {
        return ItemCategoryResponse.builder()
                .id(category.getId())
                .parentCategoryId(category.getParentCategory() != null ? category.getParentCategory().getId() : null)
                .categoryName(category.getCategoryName())
                .categoryLevel(category.getCategoryLevel())
                .sortOrder(category.getSortOrder())
                .activeYn(category.getActiveYn())
                .createdAt(category.getCreatedAt())
                .build();
    }
}
