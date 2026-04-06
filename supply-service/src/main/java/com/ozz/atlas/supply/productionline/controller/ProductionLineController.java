package com.ozz.atlas.supply.productionline.controller;

import com.ozz.atlas.supply.productionline.dtos.ProductionLineCreateDto;
import com.ozz.atlas.supply.productionline.dtos.ProductionLineResponseDto;
import com.ozz.atlas.supply.productionline.service.ProductionLineService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<ProductionLineResponseDto> createProductionLine(@Valid @RequestBody ProductionLineCreateDto dto){
        ProductionLineResponseDto response = productionLineService.createProductionLine(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }
}
