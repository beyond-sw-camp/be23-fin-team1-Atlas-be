package com.ozz.atlas.supply.lot.linemapping.controller;

import com.ozz.atlas.supply.lot.linemapping.dtos.CreateLotLineMappingRequestDto;
import com.ozz.atlas.supply.lot.linemapping.dtos.LotLineMappingResponseDto;
import com.ozz.atlas.supply.lot.linemapping.service.LotLineMappingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
