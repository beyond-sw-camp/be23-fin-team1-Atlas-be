package com.ozz.atlas.supply.sidebar.dto;

import jakarta.validation.constraints.NotBlank;

public record SupplyDetailViewRequest(
        @NotBlank String menuKey,
        @NotBlank String detailPublicId
) {
}
