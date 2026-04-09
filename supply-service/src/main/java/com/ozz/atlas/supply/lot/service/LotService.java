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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LotService {

    private final LotRepository lotRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;

    @Transactional
    public LotResponseDto createLot(CreateLotRequestDto request) {
        if (!purchaseOrderItemRepository.existsById(request.getSourcePoItemId())) {
            throw new LotException(LotErrorCode.PO_ITEM_NOT_FOUND);
        }

        Lot lot = Lot.builder()
                .lotNumber(request.getLotNumber())
                .sourcePoItemId(request.getSourcePoItemId())
                .supplierId(request.getSupplierId())
                .itemId(request.getItemId())
                .manufacturedAt(request.getManufacturedAt())
                .expiredAt(request.getExpiredAt())
                .qty(request.getQty())
                .unit(request.getUnit())
                .currentNodeId(request.getCurrentNodeId())
                .build();

        return LotResponseDto.from(lotRepository.save(lot));
    }

    public List<LotResponseDto> getAllLots() {
        return lotRepository.findAll().stream()
                .map(LotResponseDto::from)
                .collect(Collectors.toList());
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

        lot.update(request.getQty(), request.getExpiredAt(), request.getCurrentNodeId());
        return LotResponseDto.from(lot);
    }

    @Transactional
    public LotResponseDto updateLotStatus(String publicId, LotStatus lotStatus) {
        Lot lot = lotRepository.findByPublicId(publicId)
                .orElseThrow(() -> new LotException(LotErrorCode.LOT_NOT_FOUND));

        lot.changeStatus(lotStatus);
        return LotResponseDto.from(lot);
    }

    @Transactional
    public LotResponseDto updateQualityStatus(String publicId, QualityStatus qualityStatus) {
        Lot lot = lotRepository.findByPublicId(publicId)
                .orElseThrow(() -> new LotException(LotErrorCode.LOT_NOT_FOUND));

        lot.changeQuality(qualityStatus);
        return LotResponseDto.from(lot);
    }
}