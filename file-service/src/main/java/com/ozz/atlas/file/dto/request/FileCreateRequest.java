package com.ozz.atlas.file.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record FileCreateRequest(
        @NotBlank String originalName,
        @NotBlank String storageKey,
        @NotBlank String contentType,
        @NotNull @PositiveOrZero Long size
) {
}
