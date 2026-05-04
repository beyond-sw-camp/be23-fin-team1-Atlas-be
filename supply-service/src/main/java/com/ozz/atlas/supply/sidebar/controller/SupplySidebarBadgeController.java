package com.ozz.atlas.supply.sidebar.controller;

import com.ozz.atlas.supply.sidebar.dto.SupplyDetailViewRequest;
import com.ozz.atlas.supply.sidebar.dto.SupplySidebarBadgesResponse;
import com.ozz.atlas.supply.sidebar.service.SupplySidebarBadgeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/supply/sidebar")
public class SupplySidebarBadgeController {

    private final SupplySidebarBadgeService sidebarBadgeService;

    @GetMapping("/badges")
    public ResponseEntity<SupplySidebarBadgesResponse> getBadges(
            @RequestHeader(value = "X-User-Public-Id", required = false) String userPublicId,
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @RequestHeader(value = "X-Organization-Type", required = false) String organizationType
    ) {
        return ResponseEntity.ok(sidebarBadgeService.getBadges(userPublicId, organizationPublicId, organizationType));
    }

    @PostMapping("/details/read")
    public ResponseEntity<Void> markDetailViewed(
            @RequestHeader(value = "X-User-Public-Id", required = false) String userPublicId,
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @RequestHeader(value = "X-Organization-Type", required = false) String organizationType,
            @Valid @RequestBody SupplyDetailViewRequest request
    ) {
        sidebarBadgeService.markDetailViewed(
                userPublicId,
                organizationPublicId,
                organizationType,
                request.menuKey(),
                request.detailPublicId()
        );
        return ResponseEntity.noContent().build();
    }
}
