package com.ozz.atlas.supply.lot.linemapping.service;

import com.ozz.atlas.common.jpa.Status;
import com.ozz.atlas.supply.lot.linemapping.domain.LotLineMapping;
import com.ozz.atlas.supply.lot.linemapping.dtos.CreateLotLineMappingRequestDto;
import com.ozz.atlas.supply.lot.linemapping.dtos.LotLineMappingResponseDto;
import com.ozz.atlas.supply.lot.linemapping.dtos.UpdateLotLineMappingRequestDto;
import com.ozz.atlas.supply.lot.linemapping.repository.LotLineMappingRepository;
import com.ozz.atlas.supply.lot.repository.LotRepository;
import com.ozz.atlas.supply.productionline.domain.ProductionLine;
import com.ozz.atlas.supply.productionline.repository.ProductionLineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class LotLineMappingService {

    private final LotLineMappingRepository lotLineMappingRepository;

    private final LotRepository lotRepository;

    private final ProductionLineRepository productionLineRepository;

    @Autowired
    public LotLineMappingService(LotLineMappingRepository lotLineMappingRepository, LotRepository lotRepository, ProductionLineRepository productionLineRepository) {
        this.lotLineMappingRepository = lotLineMappingRepository;
        this.lotRepository = lotRepository;
        this.productionLineRepository = productionLineRepository;
    }

    //    LOT에 생산라인 매핑을 새로 등록
    public LotLineMappingResponseDto createLotLineMapping(String lotPublicId,
                                                          CreateLotLineMappingRequestDto request) {

        // LOT 존재 확인
        lotRepository.findByPublicId(lotPublicId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 LOT입니다."));

        // 생산라인 존재 및 활성 상태 확인
        ProductionLine productionLine = productionLineRepository
                .findByProductionLineIdAndStatus(request.getProductionLineId(), Status.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 비활성화된 생산라인입니다."));

        // 매핑 엔티티 생성
        LotLineMapping lotLineMapping = LotLineMapping.create(
                lotPublicId,
                productionLine,
                request.getProcessedQty(),
                request.getMappingNote()
        );

        return LotLineMappingResponseDto.fromEntity(lotLineMappingRepository.save(lotLineMapping));

    }

    //    LOT별 생산라인 매핑 목록 조회
    public List<LotLineMappingResponseDto> lotLineMappings(String lotPublicId) {

        lotRepository.findByPublicId(lotPublicId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 LOT입니다."));


        return lotLineMappingRepository.findAllByLotPublicIdOrderByLotLineMappingIdDesc(lotPublicId).stream()
                .map(LotLineMappingResponseDto::fromEntity)
                .toList();
    }

    //    단일 생산라인 매핑 상세 조회
    public LotLineMappingResponseDto getLotLineMapping(Long lotLineMappingId) {
        return LotLineMappingResponseDto.fromEntity(getLotLineMappingEntity(lotLineMappingId));
    }




    // 단일 생산라인 매핑 수정
    public LotLineMappingResponseDto updateLotLineMapping(Long lotLineMappingId,
                                                          UpdateLotLineMappingRequestDto request) {
        LotLineMapping lotLineMapping = getLotLineMappingEntity(lotLineMappingId);

        lotLineMapping.update(request.getProcessedQty(), request.getMappingNote());

        return LotLineMappingResponseDto.fromEntity(lotLineMappingRepository.save(lotLineMapping));
    }

    // 단일 생산라인 매핑 삭제
    public void deleteLotLineMapping(Long lotLineMappingId) {
        LotLineMapping lotLineMapping = getLotLineMappingEntity(lotLineMappingId);
        lotLineMappingRepository.delete(lotLineMapping);
    }

    // 단일 생산라인 매핑 엔티티 조회
    private LotLineMapping getLotLineMappingEntity(Long lotLineMappingId) {
        return lotLineMappingRepository.findById(lotLineMappingId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 LOT-생산라인 매핑입니다."));
    }

}
