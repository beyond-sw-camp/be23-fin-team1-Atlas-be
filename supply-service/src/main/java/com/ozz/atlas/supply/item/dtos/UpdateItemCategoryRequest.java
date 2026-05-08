package com.ozz.atlas.supply.item.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "날짜 요청")
public class UpdateItemCategoryRequest {

    @Size(max = 26)
    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String parentCategoryPublicId;

    @NotBlank
    @Size(max = 100)
    @Schema(description = "이름", example = "샘플 이름")
    private String categoryName;

    @Schema(description = "sort Order 값", example = "1", nullable = true)
    private Integer sortOrder;
}
