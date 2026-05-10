package com.ozz.atlas.supply.supplychain.controller;

import com.ozz.atlas.supply.supplychain.dtos.SupplyChainResponse;
import com.ozz.atlas.supply.supplychain.service.SupplyChainService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/supply/order-chains")
@Tag(name = "SupplyChain")
public class SupplyChainController {

    private final SupplyChainService supplyChainService;

    @Operation(summary = "주문 기반 공급망 체인 조회")
    @GetMapping
    public ResponseEntity<SupplyChainResponse> getSupplyChains(
            @RequestHeader("X-Organization-Public-Id") String organizationPublicId
    ) {
        return ResponseEntity.ok(supplyChainService.getSupplyChains(organizationPublicId));
    }
}
