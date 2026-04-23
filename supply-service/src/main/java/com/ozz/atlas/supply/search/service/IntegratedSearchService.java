package com.ozz.atlas.supply.search.service;

import com.ozz.atlas.supply.item.dtos.ItemResponse;
import com.ozz.atlas.supply.item.search.dtos.ItemSearchDto;
import com.ozz.atlas.supply.item.search.service.ItemSearchService;
import com.ozz.atlas.supply.lot.dtos.LotResponseDto;
import com.ozz.atlas.supply.lot.search.dtos.LotSearchDto;
import com.ozz.atlas.supply.lot.search.service.LotSearchService;
import com.ozz.atlas.supply.productionline.dtos.ProductionLineResponseDto;
import com.ozz.atlas.supply.productionline.search.dtos.ProductionLineSearchDto;
import com.ozz.atlas.supply.productionline.search.service.ProductionLineSearchService;
import com.ozz.atlas.supply.purchaseorder.domain.PurchaseOrderViewType;
import com.ozz.atlas.supply.purchaseorder.dtos.PurchaseOrderSummaryResponse;
import com.ozz.atlas.supply.purchaseorder.search.dtos.PurchaseOrderSearchDto;
import com.ozz.atlas.supply.purchaseorder.search.service.PurchaseOrderSearchService;
import com.ozz.atlas.supply.returns.dtos.ReturnRequestResponseDto;
import com.ozz.atlas.supply.returns.search.dtos.ReturnSearchDto;
import com.ozz.atlas.supply.returns.search.service.ReturnSearchService;
import com.ozz.atlas.supply.search.dtos.IntegratedSearchItemDto;
import com.ozz.atlas.supply.search.dtos.IntegratedSearchRequestDto;
import com.ozz.atlas.supply.search.dtos.IntegratedSearchResponseDto;
import com.ozz.atlas.supply.search.dtos.IntegratedSearchSectionDto;
import com.ozz.atlas.supply.search.dtos.IntegratedSearchSectionType;
import com.ozz.atlas.supply.settlement.dtos.SettlementResponseDto;
import com.ozz.atlas.supply.settlement.search.dtos.SettlementSearchDto;
import com.ozz.atlas.supply.settlement.search.service.SettlementSearchService;
import com.ozz.atlas.supply.shipment.dtos.ShipmentListResponseDto;
import com.ozz.atlas.supply.shipment.search.dtos.ShipmentSearchDto;
import com.ozz.atlas.supply.shipment.search.service.ShipmentSearchService;
import com.ozz.atlas.supply.supplier.dtos.SupplierResponse;
import com.ozz.atlas.supply.supplier.search.dtos.SupplierSearchDto;
import com.ozz.atlas.supply.supplier.search.service.SupplierSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IntegratedSearchService {


    private final SupplierSearchService supplierSearchService;
    private final ItemSearchService itemSearchService;
    private final PurchaseOrderSearchService purchaseOrderSearchService;
    private final ShipmentSearchService shipmentSearchService;
    private final ReturnSearchService returnSearchService;
    private final LotSearchService lotSearchService;
    private final ProductionLineSearchService productionLineSearchService;
    private final SettlementSearchService settlementSearchService;

    // 조직 헤더는 발주 검색처럼 조직 기준이 필요한 도메인에 사용
    public IntegratedSearchResponseDto search(
            IntegratedSearchRequestDto request,
            String organizationPublicId,
            String organizationType,
            String userRole
    ) {
        String keyword = request != null ? request.getKeyword() : null;

        // 검색어가 비어 있으면 전체 인덱스를 긁지 않고 빈 결과를 바로 반환
        if (!hasText(keyword)) {
            return IntegratedSearchResponseDto.builder()
                    .keyword(keyword)
                    .sections(List.of())
                    .build();
        }

        int size = normalizeSize(request != null ? request.getSize() : null);

        // 통합검색은 2차 필터링을 하기 때문에 처음에는 조금 더 넉넉하게 가져옴
        PageRequest pageable = PageRequest.of(0, buildFetchSize(size));

        List<IntegratedSearchSectionDto> sections = new ArrayList<>();

        // 공급사 섹션을 추가
        addSupplierSection(sections, pageable, keyword, size);

        // 품목 섹션을 추가
        addItemSection(sections, pageable, keyword, size);

        // 발주 섹션을 추가
        // 발주는 조직/뷰타입 정보가 필요해서 가능한 경우에만 조회
        addPurchaseOrderSection(sections, pageable, keyword, organizationPublicId, organizationType, size);

        // 출하 섹션을 추가
        addShipmentSection(sections, pageable, keyword, organizationPublicId, organizationType, userRole, size);

        // 반품 섹션을 추가
        addReturnSection(sections, pageable, keyword, size);

        // LOT 섹션을 추가
        addLotSection(sections, pageable, keyword, size);

        // 생산라인 섹션을 추가
        addProductionLineSection(sections, pageable, keyword, size);

        // 정산 섹션을 추가
        addSettlementSection(sections, pageable, keyword, size);

        return IntegratedSearchResponseDto.builder()
                .keyword(keyword)
                .sections(sections)
                .build();
    }

    // 공급사 검색 결과를 통합검색 섹션으로 바꿈
    private void addSupplierSection(
            List<IntegratedSearchSectionDto> sections,
            PageRequest pageable,
            String keyword,
            int size
    ) {
        Page<SupplierResponse> page = supplierSearchService.search(
                pageable,
                SupplierSearchDto.builder()
                        .keyword(keyword)
                        .build()
        );

        if (page.isEmpty()) {
            return;
        }

        // 공급사는 이름, 코드 기준으로 통합검색용 엄격 필터를 한 번 더 적용
        List<IntegratedSearchItemDto> items = page.getContent().stream()
                .filter(supplier -> matchesKeyword(keyword, supplier.getSupplierName(), supplier.getSupplierCode()))
                .map(supplier -> IntegratedSearchItemDto.builder()
                        .type(IntegratedSearchSectionType.SUPPLIER)
                        .publicId(supplier.getPublicId())
                        .title(supplier.getSupplierName())
                        .subtitle(supplier.getSupplierCode())
                        .status(supplier.getSupplierStatus() != null ? supplier.getSupplierStatus().name() : null)
                        .build())
                .limit(size)
                .toList();

        if (items.isEmpty()) {
            return;
        }

        sections.add(buildSection(IntegratedSearchSectionType.SUPPLIER, items.size(), items));
    }

    // 품목 검색 결과를 통합검색 섹션으로 바꿈
    private void addItemSection(
            List<IntegratedSearchSectionDto> sections,
            PageRequest pageable,
            String keyword,
            int size
    ) {
        Page<ItemResponse> page = itemSearchService.search(
                pageable,
                ItemSearchDto.builder()
                        .keyword(keyword)
                        .build()
        );

        if (page.isEmpty()) {
            return;
        }

        // 품목은 이름, 코드 기준으로 통합검색용 엄격 필터를 한 번 더 적용
        List<IntegratedSearchItemDto> items = page.getContent().stream()
                .filter(item -> matchesKeyword(keyword, item.getItemName(), item.getItemCode()))
                .map(item -> IntegratedSearchItemDto.builder()
                        .type(IntegratedSearchSectionType.ITEM)
                        .publicId(item.getPublicId())
                        .title(item.getItemName())
                        .subtitle(item.getItemCode())
                        .status(item.getStatus() != null ? item.getStatus().name() : null)
                        .build())
                .limit(size)
                .toList();

        if (items.isEmpty()) {
            return;
        }

        sections.add(buildSection(IntegratedSearchSectionType.ITEM, items.size(), items));
    }

    // 발주 검색 결과를 통합검색 섹션으로 바꿈
    private void addPurchaseOrderSection(
            List<IntegratedSearchSectionDto> sections,
            PageRequest pageable,
            String keyword,
            String organizationPublicId,
            String organizationType,
            int size
    ) {
        // 발주는 조직 기준 검색이 필수라서 조직 publicId가 없으면 스킵
        if (!hasText(organizationPublicId)) {
            return;
        }

        PurchaseOrderViewType viewType = resolvePurchaseOrderViewType(organizationType);

        // 조직 타입으로 발주 조회 방향을 정할 수 없으면 스킵
        if (viewType == null) {
            return;
        }

        Page<PurchaseOrderSummaryResponse> page = purchaseOrderSearchService.search(
                pageable,
                PurchaseOrderSearchDto.builder()
                        .organizationPublicId(organizationPublicId)
                        .viewType(viewType)
                        .keyword(keyword)
                        .build()
        );

        if (page.isEmpty()) {
            return;
        }

        // 발주는 발주번호, 공급사명, 공급사코드 기준으로 통합검색용 엄격 필터를 한 번 더 적용
        List<IntegratedSearchItemDto> items = page.getContent().stream()
                .filter(order -> matchesKeyword(keyword, order.getPoNumber(), order.getSupplierName(), order.getSupplierCode()))
                .map(order -> IntegratedSearchItemDto.builder()
                        .type(IntegratedSearchSectionType.PURCHASE_ORDER)
                        .publicId(order.getPoPublicId())
                        .title(order.getPoNumber())
                        .subtitle(order.getSupplierName())
                        .status(order.getPoStatus() != null ? order.getPoStatus().name() : null)
                        .build())
                .limit(size)
                .toList();

        if (items.isEmpty()) {
            return;
        }

        sections.add(buildSection(IntegratedSearchSectionType.PURCHASE_ORDER, items.size(), items));
    }

    // 출하 검색 결과를 통합검색 섹션으로 바꿈
    private void addShipmentSection(
            List<IntegratedSearchSectionDto> sections,
            PageRequest pageable,
            String keyword,
            String organizationPublicId,
            String organizationType,
            String userRole,
            int size
    ) {
        Page<ShipmentListResponseDto> page = shipmentSearchService.search(
                pageable,
                ShipmentSearchDto.builder()
                        .keyword(keyword)
                        .build(),
                organizationPublicId,
                organizationType,
                userRole
        );

        if (page.isEmpty()) {
            return;
        }

        // 출하는 출하번호, 운송사명 기준으로 통합검색용 엄격 필터를 한 번 더 적용
        List<IntegratedSearchItemDto> items = page.getContent().stream()
                .filter(shipment -> matchesKeyword(keyword, shipment.getShipmentNumber(), shipment.getCarrierName()))
                .map(shipment -> IntegratedSearchItemDto.builder()
                        .type(IntegratedSearchSectionType.SHIPMENT)
                        .publicId(shipment.getPublicId())
                        .title(shipment.getShipmentNumber())
                        .subtitle(shipment.getCarrierName())
                        .status(shipment.getStatus() != null ? shipment.getStatus().name() : null)
                        .build())
                .limit(size)
                .toList();

        if (items.isEmpty()) {
            return;
        }

        sections.add(buildSection(IntegratedSearchSectionType.SHIPMENT, items.size(), items));
    }

    // 반품 검색 결과를 통합검색 섹션으로 바꿈
    private void addReturnSection(
            List<IntegratedSearchSectionDto> sections,
            PageRequest pageable,
            String keyword,
            int size
    ) {
        Page<ReturnRequestResponseDto> page = returnSearchService.search(
                pageable,
                ReturnSearchDto.builder()
                        .keyword(keyword)
                        .build()
        );

        if (page.isEmpty()) {
            return;
        }

        // 반품은 반품번호, 반품사유 기준으로 통합검색용 엄격 필터를 한 번 더 적용
        List<IntegratedSearchItemDto> items = page.getContent().stream()
                .filter(returnRequest -> matchesKeyword(keyword, returnRequest.getReturnNumber(), returnRequest.getReturnReason()))
                .map(returnRequest -> IntegratedSearchItemDto.builder()
                        .type(IntegratedSearchSectionType.RETURN)
                        .id(returnRequest.getId())
                        .publicId(returnRequest.getPublicId())
                        .title(returnRequest.getReturnNumber())
                        .subtitle(returnRequest.getReturnReason())
                        .status(returnRequest.getReturnStatus() != null ? returnRequest.getReturnStatus().name() : null)
                        .build())
                .limit(size)
                .toList();

        if (items.isEmpty()) {
            return;
        }

        sections.add(buildSection(IntegratedSearchSectionType.RETURN, items.size(), items));
    }

    // LOT 검색 결과를 통합검색 섹션으로 바꿈
    private void addLotSection(
            List<IntegratedSearchSectionDto> sections,
            PageRequest pageable,
            String keyword,
            int size
    ) {
        Page<LotResponseDto> page = lotSearchService.search(
                pageable,
                LotSearchDto.builder()
                        .keyword(keyword)
                        .build()
        );

        if (page.isEmpty()) {
            return;
        }

        // LOT은 lot 번호, 품목 publicId 기준으로 통합검색용 엄격 필터를 한 번 더 적용
        List<IntegratedSearchItemDto> items = page.getContent().stream()
                .filter(lot -> matchesKeyword(keyword, lot.getLotNumber(), lot.getItemPublicId()))
                .map(lot -> IntegratedSearchItemDto.builder()
                        .type(IntegratedSearchSectionType.LOT)
                        .publicId(lot.getPublicId())
                        .title(lot.getLotNumber())
                        .subtitle(lot.getItemPublicId())
                        .status(lot.getLotStatus() != null ? lot.getLotStatus().name() : null)
                        .build())
                .limit(size)
                .toList();

        if (items.isEmpty()) {
            return;
        }

        sections.add(buildSection(IntegratedSearchSectionType.LOT, items.size(), items));
    }

    // 생산라인 검색 결과를 통합검색 섹션으로 바꿈
    private void addProductionLineSection(
            List<IntegratedSearchSectionDto> sections,
            PageRequest pageable,
            String keyword,
            int size
    ) {
        Page<ProductionLineResponseDto> page = productionLineSearchService.search(
                pageable,
                ProductionLineSearchDto.builder()
                        .keyword(keyword)
                        .build()
        );

        if (page.isEmpty()) {
            return;
        }

        // 생산라인은 이름, 코드 기준으로 통합검색용 엄격 필터를 한 번 더 적용
        List<IntegratedSearchItemDto> items = page.getContent().stream()
                .filter(line -> matchesKeyword(keyword, line.getLineName(), line.getLineCode()))
                .map(line -> IntegratedSearchItemDto.builder()
                        .type(IntegratedSearchSectionType.PRODUCTION_LINE)
                        .id(line.getProductionLineId())
                        .title(line.getLineName())
                        .subtitle(line.getLineCode())
                        .status(line.getStatus() != null ? line.getStatus().name() : null)
                        .build())
                .limit(size)
                .toList();

        if (items.isEmpty()) {
            return;
        }

        sections.add(buildSection(IntegratedSearchSectionType.PRODUCTION_LINE, items.size(), items));
    }

    // 정산 검색 결과를 통합검색 섹션으로 바꿈
    private void addSettlementSection(
            List<IntegratedSearchSectionDto> sections,
            PageRequest pageable,
            String keyword,
            int size
    ) {
        Page<SettlementResponseDto> page = settlementSearchService.search(
                pageable,
                SettlementSearchDto.builder()
                        .keyword(keyword)
                        .build()
        );

        if (page.isEmpty()) {
            return;
        }

        // 정산은 정산 대상 publicId 기준으로 통합검색용 엄격 필터를 한 번 더 적용
        List<IntegratedSearchItemDto> items = page.getContent().stream()
                .filter(settlement -> matchesKeyword(keyword, settlement.getTargetPublicId()))
                .map(settlement -> IntegratedSearchItemDto.builder()
                        .type(IntegratedSearchSectionType.SETTLEMENT)
                        .id(settlement.getId())
                        .title(settlement.getTargetPublicId())
                        .subtitle(settlement.getTargetType() != null ? settlement.getTargetType().name() : null)
                        .status(settlement.getSettlementStatus() != null ? settlement.getSettlementStatus().name() : null)
                        .build())
                .limit(size)
                .toList();

        if (items.isEmpty()) {
            return;
        }

        sections.add(buildSection(IntegratedSearchSectionType.SETTLEMENT, items.size(), items));
    }

    // 섹션 공통 구조를 만드는 헬퍼
    private IntegratedSearchSectionDto buildSection(
            IntegratedSearchSectionType type,
            long totalCount,
            List<IntegratedSearchItemDto> items
    ) {
        return IntegratedSearchSectionDto.builder()
                .type(type)
                .label(type.getLabel())
                .totalCount(totalCount)
                .items(items)
                .build();
    }

    // 섹션당 개수는 너무 크지 않게 제한
    private int normalizeSize(Integer size) {
        if (size == null) {
            return 5;
        }

        if (size < 1) {
            return 5;
        }

        return Math.min(size, 10);
    }

    // 통합검색은 2차 필터링을 하기 때문에 처음에는 조금 더 많이 가져옴
    private int buildFetchSize(int size) {
        return Math.max(size * 5, 20);
    }

    // 발주 검색은 현재 BUYER / SUPPLIER 두 방향만 지원
    // ADMIN은 메인 구매사 관점 조회로 간주해서 BUYER로 맞춤
    private PurchaseOrderViewType resolvePurchaseOrderViewType(String organizationType) {
        if (!hasText(organizationType)) {
            return null;
        }

        if ("SUPPLIER".equalsIgnoreCase(organizationType)) {
            return PurchaseOrderViewType.SUPPLIER;
        }

        if ("BUYER".equalsIgnoreCase(organizationType) || "ADMIN".equalsIgnoreCase(organizationType)) {
            return PurchaseOrderViewType.BUYER;
        }

        return null;
    }

    // 통합검색 전용 엄격 매칭
    // title, subtitle 같은 대표 필드에만 keyword가 실제로 들어있는지 다시 확인
    private boolean matchesKeyword(String keyword, String... values) {
        String normalizedKeyword = normalize(keyword);

        if (normalizedKeyword.isBlank()) {
            return false;
        }

        return Arrays.stream(values)
                .filter(this::hasText)
                .map(this::normalize)
                .anyMatch(value -> value.contains(normalizedKeyword));
    }

    // 대소문자, 공백, 하이픈, 언더바 차이 때문에 오탐/누락이 생기지 않게 정규화
    // 예: LINE-002 -> line002, MANUAL-SETTLEMENT-001 -> manualsettlement001
    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        return value.toLowerCase()
                .replaceAll("[\\s\\-_]", "");
    }

    // 공백이 아닌 문자열인지 확인
    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
