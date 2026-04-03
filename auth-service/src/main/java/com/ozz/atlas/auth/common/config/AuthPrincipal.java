package com.ozz.atlas.auth.common.config;

import com.ozz.atlas.auth.domain.UserRole;

public record AuthPrincipal(
        Long userId,
        String userPublicId,
        String organizationPublicId,
        UserRole role
) {
}
