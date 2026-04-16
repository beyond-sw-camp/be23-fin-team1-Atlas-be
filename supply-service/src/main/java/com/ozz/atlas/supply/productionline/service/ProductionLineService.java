package com.ozz.atlas.supply.productionline.service;

import com.ozz.atlas.common.jpa.Status;
import com.ozz.atlas.supply.logistics.repository.LogisticsNodeRepository;
import com.ozz.atlas.supply.productionline.domain.ProductionLine;
import com.ozz.atlas.supply.productionline.dtos.ProductionLineCreateDto;
import com.ozz.atlas.supply.productionline.dtos.ProductionLineResponseDto;
import com.ozz.atlas.supply.productionline.dtos.ProductionLineStatusUpdateDto;
import com.ozz.atlas.supply.productionline.dtos.ProductionLineUpdateDto;
import com.ozz.atlas.supply.productionline.repository.ProductionLineRepository;
import com.ozz.atlas.supply.productionline.search.service.ProductionLineSearchService;
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
    private final ProductionLineSearchService productionLineSearchService;


    @Autowired
    public ProductionLineService(ProductionLineRepository productionLineRepository, LogisticsNodeRepository logisticsNodeRepository, ProductionLineSearchService productionLineSearchService) {
        this.productionLineRepository = productionLineRepository;
        this.logisticsNodeRepository = logisticsNodeRepository;
        this.productionLineSearchService = productionLineSearchService;
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
        // 생성된 생산라인을 ES에도 저장
        productionLineSearchService.saveProductionLineDocument(productionLine);
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

    //    생산라인 수정
    public ProductionLineResponseDto updateProductionLine(Long productionLineId, ProductionLineUpdateDto dto) {
        ProductionLine productionLine = productionLineRepository
                .findByProductionLineIdAndStatus(productionLineId, Status.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 생산라인입니다."));


        if (productionLine.getStatus() == Status.DELETE) {
            throw new IllegalArgumentException("삭제된 생산라인은 수정할 수 없습니다.");
        }

        if (productionLine.getStatus() == Status.DEACTIVE) {
            throw new IllegalArgumentException("비활성화된 생산라인은 수정할 수 없습니다.");
        }

        if (productionLineRepository.existsByLogisticsNodePublicIdAndLineCodeAndProductionLineIdNotAndStatusIn(
                productionLine.getLogisticsNodePublicId(),
                dto.getLineCode(),
                productionLineId,
                List.of(Status.ACTIVE, Status.DEACTIVE)
        )) {
            throw new IllegalArgumentException("이미 사용 중인 생산라인 코드입니다.");
        }

        productionLine.update(
                dto.getLineCode(),
                dto.getLineName(),
                dto.getLineType(),
                dto.getDailyCapacity()
        );
        // 수정된 생산라인을 ES에도 저장
        productionLineSearchService.saveProductionLineDocument(productionLine);
        return ProductionLineResponseDto.fromEntity(productionLine);
    }

    //    생산라인 상태 변경
    public ProductionLineResponseDto updateProductionLineStatus(Long productionLineId, ProductionLineStatusUpdateDto dto) {
        ProductionLine productionLine = productionLineRepository.findById(productionLineId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 생산라인입니다."));


        if (productionLine.getStatus() == Status.DELETE) {
            throw new IllegalArgumentException("삭제된 생산라인은 상태를 변경할 수 없습니다.");
        }

        if (dto.getStatus() == Status.ACTIVE) {
            productionLine.activate();
        } else if (dto.getStatus() == Status.DEACTIVE) {
            productionLine.deactivate();
        } else {
            throw new IllegalArgumentException("상태 변경은 ACTIVE 또는 DEACTIVE만 가능합니다.");
        }

        // 상태가 바뀌었으니 ES 문서도 다시 저장
        productionLineSearchService.saveProductionLineDocument(productionLine);
        return ProductionLineResponseDto.fromEntity(productionLine);
    }

    //    생산라인 삭제
    public void deleteProductionLine(Long productionLineId) {
        ProductionLine productionLine = productionLineRepository.findById(productionLineId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 생산라인 입니다"));

        if (productionLine.getStatus() == Status.DELETE) {
            throw new IllegalArgumentException("이미 삭제된 생산라인입니다.");
        }

        productionLine.delete();
        // 삭제 상태도 ES에 반영
        productionLineSearchService.saveProductionLineDocument(productionLine);
    }

}
