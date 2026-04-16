package com.ozz.atlas.supply.lot.service;

import com.ozz.atlas.supply.lot.domain.Lot;
import com.ozz.atlas.supply.lot.domain.LotStatus;
import com.ozz.atlas.supply.lot.domain.QualityStatus;
import com.ozz.atlas.supply.lot.dtos.CreateLotRequestDto;
import com.ozz.atlas.supply.lot.dtos.LotResponseDto;
import com.ozz.atlas.supply.lot.dtos.UpdateLotRequestDto;
import com.ozz.atlas.supply.lot.exception.LotErrorCode;
import com.ozz.atlas.supply.lot.exception.LotException;
import com.ozz.atlas.supply.lot.repository.LotRepository;
import com.ozz.atlas.supply.purchaseorder.repository.PurchaseOrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import com.ozz.atlas.supply.lot.search.service.LotSearchService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LotService {

    private final LotRepository lotRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final LotSearchService lotSearchService;


    @Transactional
    public LotResponseDto createLot(CreateLotRequestDto request) {
        if (!purchaseOrderItemRepository.existsByPublicId(request.getSourcePoItemPublicId())) {
            throw new LotException(LotErrorCode.PO_ITEM_NOT_FOUND);
        }

        Lot lot = Lot.builder()
                .lotNumber(request.getLotNumber())
                .sourcePoItemPublicId(request.getSourcePoItemPublicId())
                .supplierPublicId(request.getSupplierPublicId())
                .itemPublicId(request.getItemPublicId())
                .manufacturedAt(request.getManufacturedAt())
                .expiredAt(request.getExpiredAt())
                .qty(request.getQty())
                .unit(request.getUnit())
                .currentNodePublicId(request.getCurrentNodePublicId())
                .build();

        Lot savedLot = lotRepository.save(lot);

        // 새로 생성된 LOT를 ES에도 같이 저장
        lotSearchService.saveLotDocument(savedLot);

        return LotResponseDto.from(savedLot);

    }

    public Page<LotResponseDto> getAllLots(Pageable pageable) {
        return lotRepository.findAll(pageable)
                .map(LotResponseDto::from);
    }

    public LotResponseDto getLotByPublicId(String publicId) {
        Lot lot = lotRepository.findByPublicId(publicId)
                .orElseThrow(() -> new LotException(LotErrorCode.LOT_NOT_FOUND));
        return LotResponseDto.from(lot);
    }

    @Transactional
    public LotResponseDto updateLot(String publicId, UpdateLotRequestDto request) {
        Lot lot = lotRepository.findByPublicId(publicId)
                .orElseThrow(() -> new LotException(LotErrorCode.LOT_NOT_FOUND));

        lot.update(request.getQty(), request.getExpiredAt(), request.getCurrentNodePublicId());
        // 수정된 LOT 정보를 ES에도 다시 저장
        lotSearchService.saveLotDocument(lot);
        return LotResponseDto.from(lot);
    }

    @Transactional
    public LotResponseDto updateLotStatus(String publicId, LotStatus lotStatus) {
        Lot lot = lotRepository.findByPublicId(publicId)
                .orElseThrow(() -> new LotException(LotErrorCode.LOT_NOT_FOUND));

        lot.changeStatus(lotStatus);
        // 상태가 바뀌었으니 ES 문서도 다시 저장
        lotSearchService.saveLotDocument(lot);
        return LotResponseDto.from(lot);
    }

    @Transactional
    public LotResponseDto updateQualityStatus(String publicId, QualityStatus qualityStatus) {
        Lot lot = lotRepository.findByPublicId(publicId)
                .orElseThrow(() -> new LotException(LotErrorCode.LOT_NOT_FOUND));

        lot.changeQuality(qualityStatus);
        // 품질 상태가 바뀌었으니 ES 문서도 다시 저장
        lotSearchService.saveLotDocument(lot);
        return LotResponseDto.from(lot);
    }
}