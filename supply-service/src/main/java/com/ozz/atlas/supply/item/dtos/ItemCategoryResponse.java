package com.ozz.atlas.supply.item.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Item Category 값 응답")
public class ItemCategoryResponse {

    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String publicId;
    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String parentCategoryPublicId;
    @Schema(description = "이름", example = "샘플 이름", nullable = true)
    private String categoryName;
    @Schema(description = "category Level 값", example = "1", nullable = true)
    private Integer categoryLevel;
    @Schema(description = "sort Order 값", example = "1", nullable = true)
    private Integer sortOrder;
    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String createdByOrganizationPublicId;
    @Schema(description = "상태", example = "ACTIVE", nullable = true)
    private Status status;
    @Schema(description = "생성 시각", example = "2026-05-08T10:00:00", nullable = true)
    private LocalDateTime createdAt;
    @Schema(description = "수정 시각", example = "2026-05-08T10:00:00", nullable = true)
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
                .status(category.getStatus())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
