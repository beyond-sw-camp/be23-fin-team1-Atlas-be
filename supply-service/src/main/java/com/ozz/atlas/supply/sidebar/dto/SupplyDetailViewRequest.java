package com.ozz.atlas.supply.sidebar.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Supply Detail View 값 요청")
public record SupplyDetailViewRequest(
        @NotBlank String menuKey,
        @NotBlank String detailPublicId
) {
}
