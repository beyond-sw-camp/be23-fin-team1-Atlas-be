package com.ozz.atlas.supply.sidebar.controller;

import com.ozz.atlas.supply.sidebar.dto.SupplyDetailViewRequest;
import com.ozz.atlas.supply.sidebar.dto.SupplySidebarBadgesResponse;
import com.ozz.atlas.supply.sidebar.service.SupplySidebarBadgeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "SupplySidebarBadge", description = "공급 사이드바 배지 및 상세 읽음 처리 API")
public class SupplySidebarBadgeController {

    private final SupplySidebarBadgeService sidebarBadgeService;

    @GetMapping("/badges")
    @Operation(summary = "공급 사이드바 배지 조회", description = "사용자와 조직 기준으로 공급 메뉴 사이드바 배지 정보를 조회한다.")
    public ResponseEntity<SupplySidebarBadgesResponse> getBadges(
            @RequestHeader(value = "X-User-Public-Id", required = false) String userPublicId,
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @RequestHeader(value = "X-Organization-Type", required = false) String organizationType
    ) {
        return ResponseEntity.ok(sidebarBadgeService.getBadges(userPublicId, organizationPublicId, organizationType));
    }

    @PostMapping("/details/read")
    @Operation(summary = "공급 상세 읽음 처리", description = "공급 메뉴 상세 화면 조회 이력을 저장해 배지 카운트에 반영한다.")
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
