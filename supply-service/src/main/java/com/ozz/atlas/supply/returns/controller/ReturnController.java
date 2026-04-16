package com.ozz.atlas.supply.returns.controller;

import com.ozz.atlas.supply.returns.domain.ReturnStatus;
import com.ozz.atlas.supply.returns.domain.ReturnType;
import com.ozz.atlas.supply.returns.dtos.CreateReturnRequestDto;
import com.ozz.atlas.supply.returns.dtos.ReturnRequestResponseDto;
import com.ozz.atlas.supply.returns.dtos.UpdateReturnRequestDto;
import com.ozz.atlas.supply.returns.dtos.UpdateReturnStatusDto;
import com.ozz.atlas.supply.returns.search.dtos.ReturnSearchDto;
import com.ozz.atlas.supply.returns.search.service.ReturnSearchService;
import com.ozz.atlas.supply.returns.service.ReturnService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/supply/returns")
public class ReturnController {

    private final ReturnService returnService;
    private final ReturnSearchService returnSearchService;

    @PostMapping
    public ResponseEntity<ReturnRequestResponseDto> createReturn(
            @Valid @RequestBody CreateReturnRequestDto request,
            @RequestHeader(value = "X-User-Public-Id", required = false) String actorPublicId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(returnService.createReturn(request, actorPublicId));
    }

    @GetMapping
    public ResponseEntity<?> getAllReturns(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "requestOrganizationPublicId", required = false) String requestOrganizationPublicId,
            @RequestParam(value = "targetOrganizationPublicId", required = false) String targetOrganizationPublicId,
            @RequestParam(value = "sourceShipmentPublicId", required = false) String sourceShipmentPublicId,
            @RequestParam(value = "returnType", required = false) ReturnType returnType,
            @RequestParam(value = "returnStatus", required = false) ReturnStatus returnStatus,
            @RequestParam(value = "itemPublicId", required = false) String itemPublicId,
            @RequestParam(value = "lotPublicId", required = false) String lotPublicId,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        // 컨트롤러에서 받은 검색 조건을 ES 검색 DTO로 묶습니다.
        ReturnSearchDto searchDto = ReturnSearchDto.builder()
                .keyword(keyword)
                .requestOrganizationPublicId(requestOrganizationPublicId)
                .targetOrganizationPublicId(targetOrganizationPublicId)
                .sourceShipmentPublicId(sourceShipmentPublicId)
                .returnType(returnType)
                .returnStatus(returnStatus)
                .itemPublicId(itemPublicId)
                .lotPublicId(lotPublicId)
                .build();

        // 검색 조건이 하나라도 있으면 ES 검색으로 보냄
        if (returnSearchService.hasSearchCondition(searchDto)) {
            return ResponseEntity.ok(returnSearchService.search(pageable, searchDto));
        }

        // 검색 조건이 없으면 기존 DB 페이지 목록을 사용
        return ResponseEntity.ok(returnService.getAllReturns(pageable));
    }

    @GetMapping("/{publicId}")
    public ResponseEntity<ReturnRequestResponseDto> getReturn(@PathVariable String publicId) {
        return ResponseEntity.ok(returnService.getReturnByPublicId(publicId));
    }

    @PutMapping("/{publicId}")
    public ResponseEntity<ReturnRequestResponseDto> updateReturn(
            @PathVariable String publicId,
            @Valid @RequestBody UpdateReturnRequestDto request,
            @RequestHeader(value = "X-User-Public-Id", required = false) String actorPublicId) {
        return ResponseEntity.ok(returnService.updateReturn(publicId, request, actorPublicId));
    }

    @PatchMapping("/{publicId}/status")
    public ResponseEntity<ReturnRequestResponseDto> changeStatus(
            @PathVariable String publicId,
            @Valid @RequestBody UpdateReturnStatusDto request,
            @RequestHeader(value = "X-User-Public-Id", required = false) String actorPublicId) {
        return ResponseEntity.ok(returnService.changeStatus(publicId, request, actorPublicId));
    }
}