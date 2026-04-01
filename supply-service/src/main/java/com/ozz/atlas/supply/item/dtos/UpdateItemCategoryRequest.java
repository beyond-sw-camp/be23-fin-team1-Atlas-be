package com.ozz.atlas.supply.item.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateItemCategoryRequest {
    private Long parentCategoryId;

    @NotBlank
    @Size(max = 100)
    private String categoryName;

    private Integer sortOrder;

}
