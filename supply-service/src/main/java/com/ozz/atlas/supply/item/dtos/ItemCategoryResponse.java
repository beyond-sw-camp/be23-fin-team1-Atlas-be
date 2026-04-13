package com.ozz.atlas.supply.item.dtos;

import com.ozz.atlas.common.jpa.Status;
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

    private String publicId;
    private String parentCategoryPublicId;
    private String categoryName;
    private Integer categoryLevel;
    private Integer sortOrder;
    private String createdByOrganizationPublicId;
    private String createdByUserPublicId;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ItemCategoryResponse fromEntity(SupplyItemCategory category) {
        return ItemCategoryResponse.builder()
                .publicId(category.getPublicId())
                .parentCategoryPublicId(
                        category.getParentCategory() != null ? category.getParentCategory().getPublicId() : null
                )
                .categoryName(category.getCategoryName())
                .categoryLevel(category.getCategoryLevel())
                .sortOrder(category.getSortOrder())
                .createdByOrganizationPublicId(category.getCreatedByOrganizationPublicId())
                .createdByUserPublicId(category.getCreatedByUserPublicId())
                .status(category.getStatus())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
