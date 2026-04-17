package com.ozz.atlas.gateway.search.controller;

import com.ozz.atlas.gateway.search.dtos.IntegratedSearchRequestDto;
import com.ozz.atlas.gateway.search.dtos.IntegratedSearchResponseDto;
import com.ozz.atlas.gateway.search.service.IntegratedSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
// 프론트 개발 서버(localhost:5173)에서 오는 검색 요청을 허용
// 검색 요청 전에 브라우저가 OPTIONS 프리플라이트를 보내기 때문에 OPTIONS도 같이 열어둠
// Authorization 같은 헤더가 같이 오더라도 막히지 않게 allowedHeaders 를 전체 허용
// 인증 정보나 커스텀 헤더를 같이 보낼 수 있게 allowCredentials 도 킴
@CrossOrigin(
        origins = "http://localhost:5173",
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.OPTIONS},
        allowCredentials = "true"
)
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
        // 프론트에서 받은 검색어와 size 값을 검색 요청 DTO로 묶음
        IntegratedSearchRequestDto request = IntegratedSearchRequestDto.builder()
                .keyword(keyword)
                .size(size)
                .build();

        // 인증 헤더와 조직 헤더를 그대로 넘겨서 통합검색을 수행
        return integratedSearchService.search(request, authorization, organizationPublicId, organizationType)
                .map(ResponseEntity::ok);
    }
}
