package com.ozz.atlas.supply.lot.linemapping.controller;

import com.ozz.atlas.supply.lot.linemapping.dtos.CreateLotLineMappingRequestDto;
import com.ozz.atlas.supply.lot.linemapping.dtos.LotLineMappingResponseDto;
import com.ozz.atlas.supply.lot.linemapping.dtos.UpdateLotLineMappingRequestDto;
import com.ozz.atlas.supply.lot.linemapping.service.LotLineMappingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mapping")
public class LotLineMappingController {

    private final LotLineMappingService lotLineMappingService;
    @Autowired
    public LotLineMappingController(LotLineMappingService lotLineMappingService) {
        this.lotLineMappingService = lotLineMappingService;
    }

    // LOT에 생산라인 매핑 등록
    @PostMapping("/lots/{lotPublicId}/line-mappings")
    public ResponseEntity<LotLineMappingResponseDto> createLotLineMapping(
            @PathVariable String lotPublicId,
            @Valid @RequestBody CreateLotLineMappingRequestDto request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(lotLineMappingService.createLotLineMapping(lotPublicId, request));
    }

    // LOT별 생산라인 매핑 목록 조회
    @GetMapping("/lots/{lotPublicId}/line-mappings")
    public ResponseEntity<List<LotLineMappingResponseDto>> getLotLineMappings(
            @PathVariable String lotPublicId
    ) {
        return ResponseEntity.ok(lotLineMappingService.lotLineMappings(lotPublicId));
    }

    // 단일 생산라인 매핑 상세 조회
    @GetMapping("/lot-line-mappings/{lotLineMappingId}")
    public ResponseEntity<LotLineMappingResponseDto> getLotLineMapping(
            @PathVariable Long lotLineMappingId
    ) {
        return ResponseEntity.ok(lotLineMappingService.getLotLineMapping(lotLineMappingId));
    }

    // 단일 생산라인 매핑 수정
    @PatchMapping("/lot-line-mappings/{lotLineMappingId}")
    public ResponseEntity<LotLineMappingResponseDto> updateLotLineMapping(
            @PathVariable Long lotLineMappingId,
            @Valid @RequestBody UpdateLotLineMappingRequestDto request
    ) {
        return ResponseEntity.ok(lotLineMappingService.updateLotLineMapping(lotLineMappingId, request));
    }

    // 단일 생산라인 매핑 삭제
    @DeleteMapping("/lot-line-mappings/{lotLineMappingId}")
    public ResponseEntity<Void> deleteLotLineMapping(
            @PathVariable Long lotLineMappingId
    ) {
        lotLineMappingService.deleteLotLineMapping(lotLineMappingId);
        return ResponseEntity.noContent().build();
    }
}
