package com.ozz.atlas.common.exception;

public record ErrorResponse(
        int status,
        String code,
        String message
) {
}
