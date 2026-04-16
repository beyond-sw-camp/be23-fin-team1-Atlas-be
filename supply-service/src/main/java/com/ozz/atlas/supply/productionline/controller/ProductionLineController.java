package com.ozz.atlas.supply.productionline.controller;

import com.ozz.atlas.supply.productionline.dtos.ProductionLineCreateDto;
import com.ozz.atlas.supply.productionline.dtos.ProductionLineResponseDto;
import com.ozz.atlas.supply.productionline.dtos.ProductionLineStatusUpdateDto;
import com.ozz.atlas.supply.productionline.dtos.ProductionLineUpdateDto;
import com.ozz.atlas.supply.productionline.service.ProductionLineService;
import com.ozz.atlas.common.jpa.Status;
import com.ozz.atlas.supply.productionline.search.dtos.ProductionLineSearchDto;
import com.ozz.atlas.supply.productionline.search.service.ProductionLineSearchService;
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
@RequestMapping("/api/supply/production-lines")
public class ProductionLineController {

    private final ProductionLineService productionLineService;
    private final ProductionLineSearchService productionLineSearchService;

    @Autowired
    public ProductionLineController(ProductionLineService productionLineService, ProductionLineSearchService productionLineSearchService) {
        this.productionLineService = productionLineService;
        this.productionLineSearchService = productionLineSearchService;
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
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "logisticsNodePublicId", required = false) String logisticsNodePublicId,
            @RequestParam(value = "lineType", required = false) String lineType,
            @RequestParam(value = "status", required = false) Status status,
            @PageableDefault(size = 10, sort = "productionLineId", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        ProductionLineSearchDto searchDto = ProductionLineSearchDto.builder()
                .keyword(keyword)
                .logisticsNodePublicId(logisticsNodePublicId)
                .lineType(lineType)
                .status(status)
                .build();

        if (productionLineSearchService.hasSearchCondition(searchDto)) {
            return ResponseEntity.ok(productionLineSearchService.search(pageable, searchDto));
        }

        return ResponseEntity.ok(productionLineService.productionLines(pageable));
    }

    //    생산라인 상세 조회
    @GetMapping("/{productionLineId}")
    public ResponseEntity<ProductionLineResponseDto> getProductionLine(@PathVariable Long productionLineId) {
        ProductionLineResponseDto response = productionLineService.productionLIne(productionLineId);
        return ResponseEntity.ok(response);
    }

    //    생상라인 수정
    @PatchMapping("/{productionLineId}")
    public ResponseEntity<ProductionLineResponseDto> updateProductionLine(
            @PathVariable Long productionLineId,
            @Valid @RequestBody ProductionLineUpdateDto dto) {
        return ResponseEntity.ok(productionLineService.updateProductionLine(productionLineId, dto));
    }

    //    생산라인 상태 변경
    @PatchMapping("/{productionLineId}/status")
    public ResponseEntity<ProductionLineResponseDto> updateProductionLineStatus(
            @PathVariable Long productionLineId,
            @Valid @RequestBody ProductionLineStatusUpdateDto dto) {
        return ResponseEntity.ok(productionLineService.updateProductionLineStatus(productionLineId, dto));
    }

    //    생산라인 삭제
    @DeleteMapping("/{productionLineId}")
    public ResponseEntity<Void> deleteProductionLine(@PathVariable Long productionLineId) {
        productionLineService.deleteProductionLine(productionLineId);
        return ResponseEntity.noContent().build();
    }

}
