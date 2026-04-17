package com.ozz.atlas.gateway.search.controller;

import com.ozz.atlas.gateway.search.dtos.IntegratedSearchRequestDto;
import com.ozz.atlas.gateway.search.dtos.IntegratedSearchResponseDto;
import com.ozz.atlas.gateway.search.service.IntegratedSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
public class IntegratedSearchController {

    private final IntegratedSearchService integratedSearchService;

    @GetMapping
    public Mono<ResponseEntity<IntegratedSearchResponseDto>> search(
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @RequestHeader(value = "X-Organization-Public-Id", required = false) String organizationPublicId,
            @RequestHeader(value = "X-Organization-Type", required = false) String organizationType
    ) {
        // 게이트웨이 통합검색 요청 DTO를 먼저 만듬
        IntegratedSearchRequestDto request = IntegratedSearchRequestDto.builder()
                .keyword(keyword)
                .size(size)
                .build();

        return integratedSearchService.search(request, authorization, organizationPublicId, organizationType)
                .map(ResponseEntity::ok);
    }
}
