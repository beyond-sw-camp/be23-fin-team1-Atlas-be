package com.ozz.atlas.supply.lot.controller;

import com.ozz.atlas.supply.lot.dtos.*;
import com.ozz.atlas.supply.lot.service.LotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ozz.atlas.supply.lot.domain.LotStatus;
import com.ozz.atlas.supply.lot.domain.QualityStatus;
import com.ozz.atlas.supply.lot.search.dtos.LotSearchDto;
import com.ozz.atlas.supply.lot.search.service.LotSearchService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/supply/lots")
public class LotController {

    private final LotService lotService;
    private final LotSearchService lotSearchService;

    @Operation(summary = "LOT 생성")
    @PostMapping
    public ResponseEntity<LotResponseDto> createLot(
            @Valid @RequestBody CreateLotRequestDto request,
            @RequestHeader(value = "X-User-Public-Id", required = false) String actorUserPublicId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(lotService.createLot(request, actorUserPublicId));
    }

    @Operation(summary = "LOT 목록 조회")
    @GetMapping
    public ResponseEntity<?> getAllLots(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "supplierPublicId", required = false) String supplierPublicId,
            @RequestParam(value = "itemPublicId", required = false) String itemPublicId,
            @RequestParam(value = "sourcePoItemPublicId", required = false) String sourcePoItemPublicId,
            @RequestParam(value = "currentNodePublicId", required = false) String currentNodePublicId,
            @RequestParam(value = "lotStatus", required = false) LotStatus lotStatus,
            @RequestParam(value = "qualityStatus", required = false) QualityStatus qualityStatus,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        // 컨트롤러에서 받은 검색 조건을 ES 검색 DTO로 묶음
        LotSearchDto searchDto = LotSearchDto.builder()
                .keyword(keyword)
                .supplierPublicId(supplierPublicId)
                .itemPublicId(itemPublicId)
                .sourcePoItemPublicId(sourcePoItemPublicId)
                .currentNodePublicId(currentNodePublicId)
                .lotStatus(lotStatus)
                .qualityStatus(qualityStatus)
                .build();

        // 검색 조건이 하나라도 있으면 ES 검색으로 보냄
        if (lotSearchService.hasSearchCondition(searchDto)) {
            return ResponseEntity.ok(lotSearchService.search(pageable, searchDto));
        }

        // 검색 조건이 없으면 기존 DB 페이지 목록을 사용
        return ResponseEntity.ok(lotService.getAllLots(pageable));
    }


    @Operation(summary = "LOT 상세 조회")
    @GetMapping("/{publicId}")
    public ResponseEntity<LotResponseDto> getLot(@PathVariable String publicId) {
        return ResponseEntity.ok(lotService.getLotByPublicId(publicId));
    }

    @Operation(summary = "LOT 이력 조회")
    @GetMapping("/{publicId}/histories")
    public ResponseEntity<List<LotHistoryResponseDto>> getLotHistories(@PathVariable String publicId) {
        return ResponseEntity.ok(lotService.getLotHistories(publicId));
    }

    @Operation(summary = "LOT 수정")
    @PutMapping("/{publicId}")
    public ResponseEntity<LotResponseDto> updateLot(
            @PathVariable String publicId,
            @Valid @RequestBody UpdateLotRequestDto request) {
        return ResponseEntity.ok(lotService.updateLot(publicId, request));
    }

    @Operation(summary = "LOT 상태 변경")
    @PatchMapping("/{publicId}/status")
    public ResponseEntity<LotResponseDto> updateLotStatus(
            @PathVariable String publicId,
            @RequestHeader(value = "X-User-Public-Id", required = false) String actorUserPublicId,
            @Valid @RequestBody UpdateLotStatusRequestDto request) {
        return ResponseEntity.ok(lotService.updateLotStatus(publicId, request.getLotStatus(), request.getReason(), actorUserPublicId));
    }

    @Operation(summary = "LOT 품질 상태 변경")
    @PatchMapping("/{publicId}/quality")
    public ResponseEntity<LotResponseDto> updateQualityStatus(
            @PathVariable String publicId,
            @RequestHeader(value = "X-User-Public-Id", required = false) String actorUserPublicId,
            @Valid @RequestBody UpdateQualityStatusRequestDto request) {
        return ResponseEntity.ok(lotService.updateQualityStatus(publicId, request.getQualityStatus(), request.getReason(), actorUserPublicId));
    }
}
