package com.ozz.atlas.supply.lot.controller;

import com.ozz.atlas.supply.lot.dtos.*;
import com.ozz.atlas.supply.lot.service.LotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/supply/lots")
public class LotController {

    private final LotService lotService;

    @PostMapping
    public ResponseEntity<LotResponseDto> createLot(@Valid @RequestBody CreateLotRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(lotService.createLot(request));
    }

    @GetMapping
    public ResponseEntity<List<LotResponseDto>> getAllLots() {
        return ResponseEntity.ok(lotService.getAllLots());
    }

    @GetMapping("/{publicId}")
    public ResponseEntity<LotResponseDto> getLot(@PathVariable String publicId) {
        return ResponseEntity.ok(lotService.getLotByPublicId(publicId));
    }

    @PutMapping("/{publicId}")
    public ResponseEntity<LotResponseDto> updateLot(
            @PathVariable String publicId,
            @Valid @RequestBody UpdateLotRequestDto request) {
        return ResponseEntity.ok(lotService.updateLot(publicId, request));
    }

    @PatchMapping("/{publicId}/status")
    public ResponseEntity<LotResponseDto> updateLotStatus(
            @PathVariable String publicId,
            @Valid @RequestBody UpdateLotStatusRequestDto request) {
        return ResponseEntity.ok(lotService.updateLotStatus(publicId, request.getLotStatus()));
    }

    @PatchMapping("/{publicId}/quality")
    public ResponseEntity<LotResponseDto> updateQualityStatus(
            @PathVariable String publicId,
            @Valid @RequestBody UpdateQualityStatusRequestDto request) {
        return ResponseEntity.ok(lotService.updateQualityStatus(publicId, request.getQualityStatus()));
    }
}