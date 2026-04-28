package com.ozz.atlas.supply.lot.service;

import com.ozz.atlas.common.kafka.AggregateType;
import com.ozz.atlas.common.kafka.EventTypes;
import com.ozz.atlas.common.kafka.KafkaTopics;
import com.ozz.atlas.supply.kafka.context.SupplyChainContext;
import com.ozz.atlas.supply.kafka.context.SupplyChainContextResolver;
import com.ozz.atlas.supply.kafka.event.SupplyDomainEventFactory;
import com.ozz.atlas.supply.kafka.outbox.OutboxEventAppender;
import com.ozz.atlas.supply.lot.domain.Lot;
import com.ozz.atlas.supply.lot.domain.LotStatus;
import com.ozz.atlas.supply.lot.domain.QualityStatus;
import com.ozz.atlas.supply.lot.domain.LotStatusHistory;
import com.ozz.atlas.supply.lot.dtos.CreateLotRequestDto;
import com.ozz.atlas.supply.lot.dtos.LotResponseDto;
import com.ozz.atlas.supply.lot.dtos.UpdateLotRequestDto;
import com.ozz.atlas.supply.lot.dtos.LotHistoryResponseDto;
import com.ozz.atlas.supply.lot.exception.LotErrorCode;
import com.ozz.atlas.supply.lot.exception.LotException;
import com.ozz.atlas.supply.lot.repository.LotRepository;
import com.ozz.atlas.supply.lot.repository.LotStatusHistoryRepository;
import com.ozz.atlas.supply.purchaseorder.repository.PurchaseOrderItemRepository;
import com.ozz.atlas.supply.purchaseorder.domain.SupplyPurchaseOrderItem;
import com.ozz.atlas.supply.item.repository.SupplyItemRepository;
import com.ozz.atlas.supply.item.domain.SupplyItem;
import com.ozz.atlas.supply.item.exception.ItemException;
import com.ozz.atlas.supply.item.exception.ItemErrorCode;
import com.ozz.atlas.supply.supplier.repository.SupplierRepository;
import com.ozz.atlas.supply.supplier.domain.SupplySupplier;
import com.ozz.atlas.supply.supplier.exception.SupplierException;
import com.ozz.atlas.supply.supplier.exception.SupplierErrorCode;
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
    private final SupplyItemRepository supplyItemRepository;
    private final SupplierRepository supplierRepository;
    private final LotStatusHistoryRepository lotStatusHistoryRepository;
    private final OutboxEventAppender outboxEventAppender;
    private final SupplyDomainEventFactory supplyDomainEventFactory;
    private final SupplyChainContextResolver supplyChainContextResolver;


    @Transactional
    public LotResponseDto createLot(CreateLotRequestDto request, String actorUserPublicId) {
        if (lotRepository.existsByLotNumber(request.getLotNumber())) {
            throw new LotException(LotErrorCode.DUPLICATE_LOT_NUMBER);
        }

        SupplyPurchaseOrderItem poItem = purchaseOrderItemRepository.findByPublicId(request.getSourcePoItemPublicId())
                .orElseThrow(() -> new LotException(LotErrorCode.PO_ITEM_NOT_FOUND));

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

        lot.setSourcePoItem(poItem);

        SupplyItem item = supplyItemRepository.findByPublicId(request.getItemPublicId())
                .orElseThrow(() -> new ItemException(ItemErrorCode.ITEM_NOT_FOUND));
        lot.setItem(item);

        SupplySupplier supplier = supplierRepository.findByPublicId(request.getSupplierPublicId())
                .orElseThrow(() -> new SupplierException(SupplierErrorCode.SUPPLIER_NOT_FOUND));
        lot.setSupplier(supplier);

        Lot savedLot = lotRepository.save(lot);

        LotStatusHistory history = LotStatusHistory.builder()
                .lot(savedLot)
                .lotStatus(savedLot.getLotStatus())
                .qualityStatus(savedLot.getQualityStatus())
                .currentNodePublicId(savedLot.getCurrentNodePublicId())
                .reason("LOT 생성")
                .build();
        lotStatusHistoryRepository.save(history);

        // 새로 생성된 LOT를 ES에도 같이 저장
        lotSearchService.saveLotDocument(savedLot);
        appendLotEvent(
                EventTypes.LOT_CREATED,
                savedLot,
                actorUserPublicId,
                supplier.getOrganizationPublicId(),
                "LOT 생성",
                "LOT 생성 시"
        );

        return toResponseDto(savedLot);

    }

    public Page<LotResponseDto> getAllLots(Pageable pageable) {
        return lotRepository.findAll(pageable)
                .map(this::toResponseDto);
    }

    public LotResponseDto getLotByPublicId(String publicId) {
        Lot lot = lotRepository.findByPublicId(publicId)
                .orElseThrow(() -> new LotException(LotErrorCode.LOT_NOT_FOUND));
        return toResponseDto(lot);
    }

    @Transactional
    public LotResponseDto updateLot(String publicId, UpdateLotRequestDto request) {
        Lot lot = lotRepository.findByPublicId(publicId)
                .orElseThrow(() -> new LotException(LotErrorCode.LOT_NOT_FOUND));

        String preNodePublicId = lot.getCurrentNodePublicId();
        lot.update(request.getQty(), request.getExpiredAt(), request.getCurrentNodePublicId());

        LotStatusHistory history = LotStatusHistory.builder()
                .lot(lot)
                .lotStatus(lot.getLotStatus())
                .qualityStatus(lot.getQualityStatus())
                .preNodePublicId(preNodePublicId)
                .currentNodePublicId(lot.getCurrentNodePublicId())
                .reason("LOT 정보 수정")
                .build();
        lotStatusHistoryRepository.save(history);

        // 수정된 LOT 정보를 ES에도 다시 저장
        lotSearchService.saveLotDocument(lot);
        return toResponseDto(lot);
    }

    @Transactional
    public LotResponseDto updateLotStatus(String publicId, LotStatus lotStatus, String reason, String actorUserPublicId) {
        Lot lot = lotRepository.findByPublicId(publicId)
                .orElseThrow(() -> new LotException(LotErrorCode.LOT_NOT_FOUND));

        LotStatus preLotStatus = lot.getLotStatus();
        lot.changeStatus(lotStatus);

        LotStatusHistory history = LotStatusHistory.builder()
                .lot(lot)
                .preLotStatus(preLotStatus)
                .lotStatus(lot.getLotStatus())
                .qualityStatus(lot.getQualityStatus())
                .currentNodePublicId(lot.getCurrentNodePublicId())
                .reason(reason != null && !reason.isBlank() ? reason : "상태 변경")
                .build();
        lotStatusHistoryRepository.save(history);

        // 상태가 바뀌었으니 ES 문서도 다시 저장
        lotSearchService.saveLotDocument(lot);
        appendLotEvent(
                resolveLotStatusEventType(lotStatus),
                lot,
                actorUserPublicId,
                lot.getSupplier() != null ? lot.getSupplier().getOrganizationPublicId() : null,
                "LOT 상태 변경",
                reason != null && !reason.isBlank() ? reason : "LOT 상태 변경 시"
        );
        return toResponseDto(lot);
    }

    @Transactional
    public LotResponseDto updateQualityStatus(String publicId, QualityStatus qualityStatus, String reason, String actorUserPublicId) {
        Lot lot = lotRepository.findByPublicId(publicId)
                .orElseThrow(() -> new LotException(LotErrorCode.LOT_NOT_FOUND));

        QualityStatus preQualityStatus = lot.getQualityStatus();
        lot.changeQuality(qualityStatus);

        LotStatusHistory history = LotStatusHistory.builder()
                .lot(lot)
                .lotStatus(lot.getLotStatus())
                .preQualityStatus(preQualityStatus)
                .qualityStatus(lot.getQualityStatus())
                .currentNodePublicId(lot.getCurrentNodePublicId())
                .reason(reason != null && !reason.isBlank() ? reason : "품질 상태 변경")
                .build();
        lotStatusHistoryRepository.save(history);

        // 품질 상태가 바뀌었으니 ES 문서도 다시 저장
        lotSearchService.saveLotDocument(lot);
        appendLotEvent(
                resolveQualityStatusEventType(preQualityStatus, qualityStatus),
                lot,
                actorUserPublicId,
                lot.getSupplier() != null ? lot.getSupplier().getOrganizationPublicId() : null,
                resolveQualityStatusEventName(preQualityStatus, qualityStatus),
                reason != null && !reason.isBlank()
                        ? reason
                        : resolveQualityStatusDescription(preQualityStatus, qualityStatus)
        );
        return toResponseDto(lot);
    }

    public List<LotHistoryResponseDto> getLotHistories(String publicId) {
        return lotStatusHistoryRepository.findByLot_PublicIdOrderByCreatedAtDesc(publicId)
                .stream()
                .map(LotHistoryResponseDto::from)
                .collect(Collectors.toList());
    }

    private LotResponseDto toResponseDto(Lot lot) {
        String supplierName = null;
        String itemName = null;
        if (lot.getItemPublicId() != null) {
            SupplyItem item = supplyItemRepository.findByPublicId(lot.getItemPublicId()).orElse(null);
            if (item != null) {
                itemName = item.getItemName();
                if (item.getSupplier() != null) {
                    supplierName = item.getSupplier().getSupplierName();
                }
            }
        }
        return LotResponseDto.from(lot, supplierName, itemName);
    }

    private void appendLotEvent(
            String eventType,
            Lot lot,
            String actorUserPublicId,
            String organizationPublicId,
            String eventName,
            String description
    ) {
        SupplyChainContext context = supplyChainContextResolver.fromLot(lot);
        outboxEventAppender.append(
                supplyDomainEventFactory.create(
                        KafkaTopics.SUPPLY_LOT,
                        eventType,
                        AggregateType.LOT,
                        lot.getPublicId(),
                        actorUserPublicId,
                        organizationPublicId,
                        context,
                        supplyDomainEventFactory.payload(
                                lot.getPublicId(),
                                lot.getLotNumber(),
                                lot.getLotStatus().name(),
                                eventName,
                                description,
                                null
                        )
                )
        );
    }

    private String resolveLotStatusEventType(LotStatus lotStatus) {
        if (lotStatus == LotStatus.IN_PRODUCTION) {
            return EventTypes.LOT_IN_PRODUCTION;
        }
        if (lotStatus == LotStatus.COMPLETED) {
            return EventTypes.LOT_COMPLETED;
        }
        return EventTypes.LOT_CREATED;
    }

    private String resolveQualityStatusEventType(QualityStatus previousQualityStatus, QualityStatus qualityStatus) {
        if (qualityStatus == QualityStatus.HOLD) {
            return EventTypes.LOT_HOLD;
        }
        if (qualityStatus == QualityStatus.DEFECTIVE) {
            return EventTypes.LOT_DEFECTIVE;
        }
        if (previousQualityStatus == QualityStatus.HOLD && qualityStatus == QualityStatus.NORMAL) {
            return EventTypes.LOT_RELEASED;
        }
        return EventTypes.LOT_QUALITY_PASSED;
    }

    private String resolveQualityStatusEventName(QualityStatus previousQualityStatus, QualityStatus qualityStatus) {
        if (qualityStatus == QualityStatus.HOLD) {
            return "LOT 보류";
        }
        if (qualityStatus == QualityStatus.DEFECTIVE) {
            return "LOT 불량";
        }
        if (previousQualityStatus == QualityStatus.HOLD && qualityStatus == QualityStatus.NORMAL) {
            return "LOT 보류 해제";
        }
        return "LOT 품질 통과";
    }

    private String resolveQualityStatusDescription(QualityStatus previousQualityStatus, QualityStatus qualityStatus) {
        if (qualityStatus == QualityStatus.HOLD) {
            return "LOT 보류 시";
        }
        if (qualityStatus == QualityStatus.DEFECTIVE) {
            return "LOT 불량 판정 시";
        }
        if (previousQualityStatus == QualityStatus.HOLD && qualityStatus == QualityStatus.NORMAL) {
            return "LOT 보류 해제 시";
        }
        return "LOT 품질 검사 통과 시";
    }
}
