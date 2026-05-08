package com.ozz.atlas.control.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
@Schema(description = "Auth User Recipient 값 모델")
public record AuthUserRecipientDto(
        String userPublicId,
        String organizationPublicId,
        String departmentCode,
        String userRole
) {
}
