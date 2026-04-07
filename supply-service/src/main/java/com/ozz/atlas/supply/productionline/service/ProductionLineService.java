package com.ozz.atlas.supply.productionline.service;

import com.ozz.atlas.common.jpa.Status;
import com.ozz.atlas.supply.logistics.repository.LogisticsNodeRepository;
import com.ozz.atlas.supply.productionline.domain.ProductionLine;
import com.ozz.atlas.supply.productionline.dtos.ProductionLineCreateDto;
import com.ozz.atlas.supply.productionline.dtos.ProductionLineResponseDto;
import com.ozz.atlas.supply.productionline.repository.ProductionLineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductionLineService {

    private final ProductionLineRepository productionLineRepository;
    private final LogisticsNodeRepository logisticsNodeRepository;

    @Autowired
    public ProductionLineService(ProductionLineRepository productionLineRepository, LogisticsNodeRepository logisticsNodeRepository) {
        this.productionLineRepository = productionLineRepository;
        this.logisticsNodeRepository = logisticsNodeRepository;
    }

    //    생산라인 등록
    public ProductionLineResponseDto createProductionLine(ProductionLineCreateDto dto) {
        logisticsNodeRepository.findByPublicId(dto.getLogisticsNodePublicId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 물류거점입니다."));

        if (productionLineRepository.existsByLogisticsNodePublicIdAndLineCodeAndStatusIn(
                dto.getLogisticsNodePublicId(),
                dto.getLineCode(),
                List.of(Status.ACTIVE, Status.DEACTIVE)
        )) {
            throw new IllegalArgumentException("이미 사용 중인 생산라인 코드입니다.");
        }

        ProductionLine productionLine = productionLineRepository.save(dto.toEntity());
        return ProductionLineResponseDto.fromEntity(productionLine);
    }

    //    생산라인 목록 조회
    public Page<ProductionLineResponseDto> productionLines(Pageable pageable) {
        return productionLineRepository.findAllByStatus(Status.ACTIVE, pageable)
                .map(ProductionLineResponseDto::fromEntity);

    }

    //    생산라인 상세 조회
    public ProductionLineResponseDto productionLIne(Long productionLineId) {
        ProductionLine productionLine = productionLineRepository
                .findByProductionLineIdAndStatus(productionLineId, Status.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 생산라인 입니다"));

        return ProductionLineResponseDto.fromEntity(productionLine);

    }
}
