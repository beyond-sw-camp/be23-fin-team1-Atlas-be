package com.ozz.atlas.supply.returns.controller;

import com.ozz.atlas.supply.returns.dtos.CreateReturnRequestDto;
import com.ozz.atlas.supply.returns.dtos.ReturnRequestResponseDto;
import com.ozz.atlas.supply.returns.dtos.UpdateReturnRequestDto;
import com.ozz.atlas.supply.returns.dtos.UpdateReturnStatusDto;
import com.ozz.atlas.supply.returns.service.ReturnService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/supply/returns")
public class ReturnController {

    private final ReturnService returnService;

    @PostMapping
    public ResponseEntity<ReturnRequestResponseDto> createReturn(
            @Valid @RequestBody CreateReturnRequestDto request,
            @RequestHeader(value = "X-User-Public-Id", required = false) String actorPublicId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(returnService.createReturn(request, actorPublicId));
    }

    @GetMapping
    public ResponseEntity<List<ReturnRequestResponseDto>> getAllReturns() {
        return ResponseEntity.ok(returnService.getAllReturns());
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