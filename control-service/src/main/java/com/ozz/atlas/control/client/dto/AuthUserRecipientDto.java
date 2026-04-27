package com.ozz.atlas.control.client.dto;

public record AuthUserRecipientDto(
        String userPublicId,
        String organizationPublicId,
        String departmentCode,
        String userRole
) {
}
