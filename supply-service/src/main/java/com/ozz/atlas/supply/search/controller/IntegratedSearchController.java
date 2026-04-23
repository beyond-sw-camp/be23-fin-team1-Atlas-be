package com.ozz.atlas.supply.search.controller;

import com.ozz.atlas.supply.search.dtos.IntegratedSearchRequestDto;
import com.ozz.atlas.supply.search.dtos.IntegratedSearchResponseDto;
import com.ozz.atlas.supply.search.service.IntegratedSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/supply/search")
public class IntegratedSearchController {

    private final IntegratedSearchService integratedSearchService;

    @GetMapping
    public ResponseEntity<IntegratedSearchResponseDto> search(
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @RequestHeader(value = "X-Organization-Type", required = false) String organizationType,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        IntegratedSearchRequestDto request = IntegratedSearchRequestDto.builder()
                .keyword(keyword)
                .size(size)
                .build();

        return ResponseEntity.ok(
                integratedSearchService.search(request, organizationPublicId, organizationType, userRole)
        );
    }
}
