package com.ozz.atlas.supply.productionline.controller;

import com.ozz.atlas.supply.productionline.dtos.ProductionLineCreateDto;
import com.ozz.atlas.supply.productionline.dtos.ProductionLineResponseDto;
import com.ozz.atlas.supply.productionline.service.ProductionLineService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/production-lines")
public class ProductionLineController {

    private final ProductionLineService productionLineService;

    @Autowired
    public ProductionLineController(ProductionLineService productionLineService) {
        this.productionLineService = productionLineService;
    }

    //    생산라인 등록
    @PostMapping("/create")
    public ResponseEntity<ProductionLineResponseDto> createProductionLine(@Valid @RequestBody ProductionLineCreateDto dto) {
        ProductionLineResponseDto response = productionLineService.createProductionLine(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //   생산라인 목록 조회
    @GetMapping
    public ResponseEntity<Page<ProductionLineResponseDto>> getProductionLines(
            @PageableDefault(size = 10, sort = "productionLineId", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<ProductionLineResponseDto> response = productionLineService.productionLines(pageable);
        return ResponseEntity.ok(response);
    }

    //    생산라인 상세 조회
    @GetMapping("/{productionLineId}")
    public ResponseEntity<ProductionLineResponseDto> getProductionLine(@PathVariable Long productionLineId) {
        ProductionLineResponseDto response = productionLineService.productionLIne(productionLineId);
        return ResponseEntity.ok(response);
    }

}
