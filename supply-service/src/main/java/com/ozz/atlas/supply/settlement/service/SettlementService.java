package com.ozz.atlas.supply.settlement.service;

import com.ozz.atlas.supply.purchaseorder.domain.SupplyPurchaseOrder;
import com.ozz.atlas.supply.purchaseorder.domain.SupplyPurchaseOrderItem;
import com.ozz.atlas.supply.purchaseorder.repository.PurchaseOrderRepository;
import com.ozz.atlas.supply.returns.domain.ReturnItem;
import com.ozz.atlas.supply.returns.domain.ReturnRequest;
import com.ozz.atlas.supply.returns.domain.ReturnStatus;
import com.ozz.atlas.supply.returns.repository.ReturnRequestRepository;
import com.ozz.atlas.supply.settlement.domain.*;
import com.ozz.atlas.supply.settlement.dtos.*;
import com.ozz.atlas.supply.settlement.repository.SettlementBudgetRepository;
import com.ozz.atlas.supply.settlement.search.service.SettlementSearchService;
import com.ozz.atlas.supply.settlement.exception.SettlementErrorCode;
import com.ozz.atlas.supply.settlement.exception.SettlementException;
import com.ozz.atlas.supply.settlement.repository.SettlementDetailRepository;
import com.ozz.atlas.supply.settlement.repository.SettlementRepository;
import com.ozz.atlas.supply.shipment.domain.Shipment;
import com.ozz.atlas.supply.shipment.repository.ShipmentLineRepository;
import com.ozz.atlas.supply.shipment.repository.ShipmentRepository;
import com.ozz.atlas.supply.supplier.domain.SupplierStatus;
import com.ozz.atlas.supply.supplier.domain.SupplySupplier;
import com.ozz.atlas.supply.supplier.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ozz.atlas.supply.logistics.domain.LogisticsNode;
import com.ozz.atlas.supply.logistics.repository.LogisticsNodeRepository;
import com.ozz.atlas.supply.shipment.domain.ShipmentStatus;
import com.ozz.atlas.supply.subpurchaseorder.domain.SupplySubPurchaseOrder;
import com.ozz.atlas.supply.subpurchaseorder.domain.SupplySubPurchaseOrderItem;
import com.ozz.atlas.supply.subpurchaseorder.repository.SubPurchaseOrderRepository;
import com.ozz.atlas.supply.settlement.domain.BudgetUsageStatus;
import com.ozz.atlas.supply.settlement.dtos.SettlementBudgetUsageDto;
import com.ozz.atlas.common.excel.ExcelExportUtils;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import com.ozz.atlas.supply.purchaseorder.domain.PoStatus;


import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final SettlementDetailRepository settlementDetailRepository;
    private final SupplierRepository supplierRepository;
    private final ReturnRequestRepository returnRequestRepository;
    private final ShipmentRepository shipmentRepository;
    private final ShipmentLineRepository shipmentLineRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SubPurchaseOrderRepository subPurchaseOrderRepository;
    private final LogisticsNodeRepository logisticsNodeRepository;
    private final SettlementSearchService settlementSearchService;
    private final SettlementBudgetRepository settlementBudgetRepository;
    private final SettlementOrganizationClient settlementOrganizationClient;



    @Transactional(readOnly = true)
    public String getSettlementPublicIdByTargetPublicId(String targetPublicId, SettlementTargetType targetType) {
        return settlementRepository.findByTargetTypeAndTargetPublicIdAndSettlementStatusNot(
                targetType,
                targetPublicId,
                SettlementStatus.CANCELLED
        ).map(Settlement::getPublicId).orElse(null);
    }

    // 정산 생성 -> 상세 금액 합계를 헤더 amount에 반영
    @Transactional
    public SettlementResponseDto createSettlement(
            CreateSettlementRequestDto request,
            String actorOrganizationPublicId,
            String userRole
    ) {
        validateSettlementActorHeader(actorOrganizationPublicId, userRole);
        validateCreateRequest(request);

        SettlementContext context = resolveSettlementContext(request, actorOrganizationPublicId);

        Settlement settlement = Settlement.builder()
                .buyerOrganizationPublicId(context.buyerOrganizationPublicId())
                .supplierOrganizationPublicId(context.supplierOrganizationPublicId())
                .supplierId(context.supplier().getId())
                .targetType(request.getTargetType())
                .targetPublicId(request.getTargetPublicId())
                .settlementPeriodStart(request.getSettlementPeriodStart())
                .settlementPeriodEnd(request.getSettlementPeriodEnd())
                .currencyCode(request.getCurrencyCode())
                .amount(BigDecimal.ZERO)
                .build();

        Settlement savedSettlement = settlementRepository.save(settlement);

        List<SettlementDetail> details;
        if (request.getTargetType() == SettlementTargetType.RETURN) {
            details = createReturnSettlementDetails(savedSettlement, request);
        } else if (request.getTargetType() == SettlementTargetType.ORDER) {
            details = createOrderSettlementDetails(savedSettlement, request.getTargetPublicId());
        } else {
            details = createShipmentSettlementDetails(savedSettlement, request.getTargetPublicId());
        }

        List<SettlementDetail> savedDetails = settlementDetailRepository.saveAll(details);

        BigDecimal totalAmount = savedDetails.stream()
                .map(SettlementDetail::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        savedSettlement.updateAmount(totalAmount);

        settlementSearchService.saveSettlementDocument(savedSettlement);

        return toResponseDto(savedSettlement, context.supplier().getPublicId(), savedDetails);
    }

    @Transactional
    public void createShipmentSettlementIfAbsent(String shipmentPublicId) {
        Shipment shipment = getShipmentByPublicId(shipmentPublicId);

        if (shipment.getStatus() != ShipmentStatus.ARRIVED) {
            return;
        }

        if (shipment.getPoId() != null && shipment.getPurchaseOrderPublicId() != null) {
            createPurchaseOrderSettlementIfReady(shipment);
            return;
        }

        if (settlementRepository.existsByTargetTypeAndTargetPublicIdAndSettlementStatusNot(
                SettlementTargetType.SHIPMENT,
                shipmentPublicId,
                SettlementStatus.CANCELLED
        )) {
            return;
        }

        LogisticsNode destinationNode = getLogisticsNodeById(shipment.getDestinationNodeId());
        createSettlement(
                CreateSettlementRequestDto.builder()
                        .targetType(SettlementTargetType.SHIPMENT)
                        .targetPublicId(shipmentPublicId)
                        .currencyCode(resolveShipmentCurrencyCode(shipment))
                        .build(),
                destinationNode.getOrganizationPublicId(),
                "SYSTEM"
        );
    }

    @Transactional
    public void createReturnSettlementIfAbsent(String returnPublicId) {
        if (settlementRepository.existsByTargetTypeAndTargetPublicIdAndSettlementStatusNot(
                SettlementTargetType.RETURN,
                returnPublicId,
                SettlementStatus.CANCELLED
        )) {
            return;
        }

        ReturnRequest returnRequest = returnRequestRepository.findByPublicId(returnPublicId)
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.RETURN_NOT_FOUND));

        if (returnRequest.getResolutionType() == com.ozz.atlas.supply.returns.domain.ResolutionType.EXCHANGE) {
            // EXCHANGE는 상품 대금 차감 정산이 발생하지 않음. 물류비 정산은 별도 로직 또는 수기 처리 필요.
            return;
        }

        createSettlement(
                CreateSettlementRequestDto.builder()
                        .targetType(SettlementTargetType.RETURN)
                        .targetPublicId(returnPublicId)
                        .currencyCode(resolveReturnCurrencyCode(returnRequest))
                        .build(),
                returnRequest.getRequestOrganizationPublicId(),
                "SYSTEM"
        );
    }

    // 정산 목록 조회
    @Transactional(readOnly = true)
    public Page<SettlementResponseDto> getSettlements(
            Pageable pageable,
            String actorOrganizationPublicId,
            String userRole
    ) {
        validateSettlementActorHeader(actorOrganizationPublicId, userRole);

        return settlementRepository.findReadableByOrganizationPublicId(actorOrganizationPublicId, pageable)
                .map(this::toResponseDtoWithoutDetails);
    }

    @Transactional(readOnly = true)
    public byte[] exportSettlementExcel(
            String actorOrganizationPublicId,
            String actorUserPublicId,
            String userRole,
            String language,
            LocalDate startDate,
            LocalDate endDate
    ) {
        validateSettlementActorHeader(actorOrganizationPublicId, userRole);

        List<Settlement> settlements = filterSettlementsByExcelPeriod(
                settlementRepository.findAllReadableByOrganizationPublicId(actorOrganizationPublicId),
                startDate, endDate
        );

        BigDecimal totalAmount = settlements.stream()
                .map(Settlement::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Set<String> organizationPublicIds = new HashSet<>();
        for (Settlement settlement : settlements) {
            if (settlement.getBuyerOrganizationPublicId() != null)
                organizationPublicIds.add(settlement.getBuyerOrganizationPublicId());
            if (settlement.getSupplierOrganizationPublicId() != null)
                organizationPublicIds.add(settlement.getSupplierOrganizationPublicId());
        }
        Map<String, OrganizationNameLookupResponseDto> organizationMap =
                loadOrganizationMap(organizationPublicIds);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(isKorean(language) ? "정산 내역" : "Settlement Ledger");


            OrganizationNameLookupResponseDto actorOrganization =
                    settlementOrganizationClient.getOrganization(actorOrganizationPublicId);

            String actorOrganizationName = actorOrganization.getOrganizationName() == null
                    || actorOrganization.getOrganizationName().isBlank()
                    ? actorOrganizationPublicId : actorOrganization.getOrganizationName();

            String representativeName = joinOrganizationContactName(
                    actorOrganization.getContactFirstName(),
                    actorOrganization.getContactMiddleName(),
                    actorOrganization.getContactLastName());
            if (representativeName.isBlank()) representativeName = "-";

            String organizationPhone = actorOrganization.getContactPhone() == null
                    || actorOrganization.getContactPhone().isBlank()
                    ? "-" : actorOrganization.getContactPhone();

            String issuedDate = LocalDate.now().toString();

            UserNameLookupResponseDto exporter = settlementOrganizationClient.getUserName(actorUserPublicId);
            String exporterName = exporter.getUserName() == null || exporter.getUserName().isBlank()
                    ? "-" : exporter.getUserName();


            CellStyle titleStyle        = ExcelExportUtils.createDocumentTitleStyle(workbook);
            CellStyle issuedDateStyle   = ExcelExportUtils.createIssuedDateStyle(workbook);
            CellStyle sectionTitleStyle = ExcelExportUtils.createSectionTitleStyle(workbook);
            CellStyle infoLabelStyle    = ExcelExportUtils.createInfoLabelStyle(workbook);
            CellStyle infoValueStyle    = ExcelExportUtils.createInfoValueStyle(workbook);
            CellStyle summaryLabelStyle = ExcelExportUtils.createSummaryLabelStyle(workbook);
            CellStyle summaryAmountStyle= ExcelExportUtils.createSummaryAmountStyle(workbook);
            CellStyle tableHeaderStyle  = ExcelExportUtils.createSettlementTableHeaderStyle(workbook);
            CellStyle tableBodyStyle    = ExcelExportUtils.createSettlementTableBodyStyle(workbook);
            CellStyle tableAmountStyle  = ExcelExportUtils.createSettlementTableAmountStyle(workbook);
            CellStyle totalLabelStyle   = ExcelExportUtils.createSettlementTotalLabelStyle(workbook);
            CellStyle totalAmountStyle  = ExcelExportUtils.createSettlementTotalAmountStyle(workbook);


            sheet.createRow(0).setHeightInPoints(8);
            sheet.createRow(1).setHeightInPoints(40);
            sheet.createRow(2).setHeightInPoints(18);
            sheet.createRow(3).setHeightInPoints(6);
            sheet.createRow(4).setHeightInPoints(22);
            sheet.createRow(5).setHeightInPoints(4);
            sheet.createRow(6).setHeightInPoints(20);
            sheet.createRow(7).setHeightInPoints(20);
            sheet.createRow(8).setHeightInPoints(20);
            sheet.createRow(9).setHeightInPoints(20);
            sheet.createRow(10).setHeightInPoints(6);
            sheet.createRow(11).setHeightInPoints(24);
            sheet.createRow(12).setHeightInPoints(6);
            sheet.createRow(13).setHeightInPoints(20);
            sheet.createRow(14).setHeightInPoints(8);
            sheet.createRow(15).setHeightInPoints(26);


            sheet.setColumnWidth(0, (int)(4   * 256));
            sheet.setColumnWidth(1, (int)(14  * 256));
            sheet.setColumnWidth(2, (int)(20  * 256));
            sheet.setColumnWidth(3, (int)(20  * 256));
            sheet.setColumnWidth(4, (int)(12  * 256));
            sheet.setColumnWidth(5, (int)(12  * 256));
            sheet.setColumnWidth(6, (int)(8   * 256));
            sheet.setColumnWidth(7, (int)(8   * 256));
            sheet.setColumnWidth(8, (int)(8   * 256));
            sheet.setColumnWidth(9, (int)(8   * 256));
            sheet.setColumnWidth(10,(int)(4   * 256));


            ExcelExportUtils.writeMergedCell(sheet, 1, 1, 9,
                    isKorean(language) ? "정  산  내  역  서" : "Settlement Statement",
                    titleStyle);


            ExcelExportUtils.writeMergedCell(sheet, 2, 5, 9,
                    (isKorean(language) ? "발행일 : " : "Issued Date : ") + issuedDate,
                    issuedDateStyle);


            ExcelExportUtils.writeMergedCell(sheet, 4, 1, 4,
                    isKorean(language) ? "수  급  인" : "Recipient",
                    sectionTitleStyle);

            String[][] infoRows = {
                    {isKorean(language) ? "회  사  명" : "Company",        actorOrganizationName},
                    {isKorean(language) ? "대  표  자" : "Representative", representativeName},
                    {isKorean(language) ? "연  락  처" : "Phone",          organizationPhone},
                    {isKorean(language) ? "담  당  자" : "Exporter",       exporterName},
            };
            for (int i = 0; i < infoRows.length; i++) {
                int r = 6 + i;
                ExcelExportUtils.writeCell(sheet.getRow(r), 1, infoRows[i][0], infoLabelStyle);
                ExcelExportUtils.writeMergedCell(sheet, r, 2, 9, infoRows[i][1], infoValueStyle);
            }


            ExcelExportUtils.writeMergedCell(sheet, 11, 1, 3,
                    isKorean(language) ? "총  정  산  금  액" : "Total Settlement Amount",
                    summaryLabelStyle);
            ExcelExportUtils.writeMergedCell(sheet, 11, 4, 9, totalAmount, summaryAmountStyle);


            ExcelExportUtils.writeMergedCell(sheet, 13, 1, 3,
                    isKorean(language) ? "정  산  기  간" : "Settlement Period",
                    infoLabelStyle);
            ExcelExportUtils.writeMergedCell(sheet, 13, 4, 9,
                    settlementPeriodText(startDate, endDate), infoValueStyle);


            Row headerRow = sheet.getRow(15);
            ExcelExportUtils.writeCell(headerRow, 1, isKorean(language) ? "날짜" : "Date", tableHeaderStyle);
            ExcelExportUtils.writeCell(headerRow, 2, isKorean(language) ? "발주 조직명" : "Buyer Organization", tableHeaderStyle);
            ExcelExportUtils.writeCell(headerRow, 3, isKorean(language) ? "협력사 조직명" : "Supplier Organization", tableHeaderStyle);
            ExcelExportUtils.writeCell(headerRow, 4, isKorean(language) ? "대상 유형" : "Target Type", tableHeaderStyle);
            ExcelExportUtils.writeCell(headerRow, 5, isKorean(language) ? "정산 상태" : "Status", tableHeaderStyle);
            ExcelExportUtils.writeMergedCell(sheet, 15, 6, 8,
                    isKorean(language) ? "정산 금액" : "Settlement Amount", tableHeaderStyle);
            ExcelExportUtils.writeCell(headerRow, 9, isKorean(language) ? "통화" : "Currency", tableHeaderStyle);

            sheet.createFreezePane(0, 16);


            int rowIndex = 16;
            for (Settlement settlement : settlements) {
                Row row = sheet.createRow(rowIndex);
                row.setHeightInPoints(19);

                ExcelExportUtils.writeCell(row, 1, settlementExcelDate(settlement), tableBodyStyle);
                ExcelExportUtils.writeCell(row, 2, organizationDisplayName(settlement.getBuyerOrganizationPublicId(), organizationMap), tableBodyStyle);
                ExcelExportUtils.writeCell(row, 3, organizationDisplayName(settlement.getSupplierOrganizationPublicId(), organizationMap), tableBodyStyle);
                ExcelExportUtils.writeCell(row, 4, targetTypeText(settlement.getTargetType(), language), tableBodyStyle);
                ExcelExportUtils.writeCell(row, 5, statusText(settlement.getSettlementStatus(), language), tableBodyStyle);
                ExcelExportUtils.writeMergedCell(sheet, rowIndex, 6, 8, settlement.getAmount(), tableAmountStyle);
                ExcelExportUtils.writeCell(row, 9,
                        settlement.getCurrencyCode() != null ? settlement.getCurrencyCode().name() : "",
                        tableBodyStyle);
                rowIndex++;
            }


            Row totalTableRow = sheet.createRow(rowIndex);
            totalTableRow.setHeightInPoints(24);
            ExcelExportUtils.writeMergedCell(sheet, rowIndex, 1, 5,
                    isKorean(language) ? "합  계" : "Total", totalLabelStyle);
            ExcelExportUtils.writeMergedCell(sheet, rowIndex, 6, 8, totalAmount, totalAmountStyle);
            ExcelExportUtils.writeCell(totalTableRow, 9,
                    settlements.isEmpty() || settlements.get(0).getCurrencyCode() == null
                            ? "" : settlements.get(0).getCurrencyCode().name(),
                    totalLabelStyle);

            return ExcelExportUtils.toByteArray(workbook);
        } catch (Exception e) {
            throw new IllegalStateException("정산 엑셀 파일 생성에 실패했습니다.", e);
        }
    }

    private boolean isKorean(String language) {
        return language == null || language.isBlank() || "ko".equalsIgnoreCase(language);
    }

    private String joinOrganizationContactName(
            String firstName,
            String middleName,
            String lastName
    ) {
        List<String> names = new ArrayList<>();

        if (lastName != null && !lastName.isBlank()) {
            names.add(lastName);
        }

        if (firstName != null && !firstName.isBlank()) {
            names.add(firstName);
        }

        if (middleName != null && !middleName.isBlank()) {
            names.add(middleName);
        }

        return String.join("", names);
    }


    private String settlementExcelDate(Settlement settlement) {
        LocalDate settlementDate = settlementExcelLocalDate(settlement);

        return settlementDate == null ? "" : settlementDate.toString();
    }


    private LocalDate settlementExcelLocalDate(Settlement settlement) {
        if (settlement.getSettlementPeriodStart() != null) {
            return settlement.getSettlementPeriodStart();
        }

        if (settlement.getCreatedAt() != null) {
            return settlement.getCreatedAt().toLocalDate();
        }

        return null;
    }


    private List<Settlement> filterSettlementsByExcelPeriod(
            List<Settlement> settlements,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return settlements.stream()
                .filter(settlement -> isInExcelPeriod(settlement, startDate, endDate))
                .toList();
    }


    private boolean isInExcelPeriod(
            Settlement settlement,
            LocalDate startDate,
            LocalDate endDate
    ) {
        LocalDate settlementDate = settlementExcelLocalDate(settlement);

        if (settlementDate == null) {
            return startDate == null && endDate == null;
        }

        if (startDate != null && settlementDate.isBefore(startDate)) {
            return false;
        }

        if (endDate != null && settlementDate.isAfter(endDate)) {
            return false;
        }

        return true;
    }

    private String settlementPeriodText(LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return "ALL";
        }

        String startText = startDate == null ? "" : startDate.toString();
        String endText = endDate == null ? "" : endDate.toString();

        return startText + " ~ " + endText;
    }


    private Map<String, OrganizationNameLookupResponseDto> loadOrganizationMap(Set<String> organizationPublicIds) {
        Map<String, OrganizationNameLookupResponseDto> result = new HashMap<>();

        for (String organizationPublicId : organizationPublicIds) {
            if (organizationPublicId == null || organizationPublicId.isBlank()) {
                continue;
            }

            OrganizationNameLookupResponseDto organization =
                    settlementOrganizationClient.getOrganization(organizationPublicId);

            result.put(organizationPublicId, organization);
        }

        return result;
    }


    private String organizationDisplayName(
            String organizationPublicId,
            Map<String, OrganizationNameLookupResponseDto> organizationMap
    ) {
        if (organizationPublicId == null || organizationPublicId.isBlank()) {
            return "";
        }

        OrganizationNameLookupResponseDto organization = organizationMap.get(organizationPublicId);

        if (organization == null ||
                organization.getOrganizationName() == null ||
                organization.getOrganizationName().isBlank()) {
            return organizationPublicId;
        }

        return organization.getOrganizationName();
    }


    private String targetTypeText(SettlementTargetType targetType, String language) {
        if (targetType == null) {
            return "";
        }

        if (!isKorean(language)) {
            return switch (targetType) {
                case ORDER -> "Order";
                case SHIPMENT -> "Shipment";
                case RETURN -> "Return";
                case DELIVERY_EXCEPTION -> "Delivery Exception";
            };
        }

        return switch (targetType) {
            case ORDER -> "발주";
            case SHIPMENT -> "출하";
            case RETURN -> "반품";
            case DELIVERY_EXCEPTION -> "배송 예외";
        };
    }

    private String statusText(SettlementStatus status, String language) {
        if (status == null) {
            return "";
        }

        if (!isKorean(language)) {
            return switch (status) {
                case PENDING -> "Pending";
                case APPROVED -> "Approved";
                case CANCELLED -> "Cancelled";
            };
        }

        return switch (status) {
            case PENDING -> "대기";
            case APPROVED -> "승인";
            case CANCELLED -> "취소";
        };
    }


    @Transactional(readOnly = true)
    public Page<SettlementResponseDto> searchSettlements(
            Pageable pageable,
            com.ozz.atlas.supply.settlement.search.dtos.SettlementSearchDto searchDto,
            String actorOrganizationPublicId,
            String userRole
    ) {
        validateSettlementActorHeader(actorOrganizationPublicId, userRole);

        return settlementSearchService.search(pageable, searchDto, actorOrganizationPublicId);
    }

    // 정산 상세 조회
    @Transactional(readOnly = true)
    public SettlementResponseDto getSettlement(
            String settlementPublicId,
            String actorOrganizationPublicId,
            String userRole
    ) {
        validateSettlementActorHeader(actorOrganizationPublicId, userRole);

        Settlement settlement = settlementRepository.findReadableByPublicId(
                        settlementPublicId,
                        actorOrganizationPublicId
                )
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.SETTLEMENT_NOT_FOUND));

        List<SettlementDetail> details =
                settlementDetailRepository.findAllBySettlement_IdOrderByIdAsc(settlement.getId());

        String supplierPublicId = getSupplierPublicId(settlement.getSupplierId());

        return toResponseDto(settlement, supplierPublicId, details);
    }

    // 정산 승인 -> 연결된 모든 상세 항목 승인 상태로 전환
    @Transactional
    public SettlementResponseDto approveSettlement(
            String settlementPublicId,
            String actorOrganizationPublicId,
            String approvedByUserPublicId,
            String userRole
    ) {
        validateSettlementActionHeader(actorOrganizationPublicId, approvedByUserPublicId, userRole);

        Settlement settlement = settlementRepository.findReadableByPublicId(
                        settlementPublicId,
                        actorOrganizationPublicId
                )
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.SETTLEMENT_NOT_FOUND));

        if (!actorOrganizationPublicId.equals(settlement.getSupplierOrganizationPublicId())) {
            throw new SettlementException(SettlementErrorCode.FORBIDDEN_SETTLEMENT_APPROVAL);
        }

        try {
            settlement.approve(approvedByUserPublicId);
        } catch (IllegalStateException e) {
            throw new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_STATUS_TRANSITION);
        }

        List<SettlementDetail> details =
                settlementDetailRepository.findAllBySettlement_IdOrderByIdAsc(settlement.getId());

        details.forEach(SettlementDetail::approve);

        settlementSearchService.saveSettlementDocument(settlement);

        String supplierPublicId = getSupplierPublicId(settlement.getSupplierId());

        return toResponseDto(settlement, supplierPublicId, details);
    }

    // 정산 취소 -> 연결된 모든 상세 항목 취소 상태로 전환
    @Transactional
    public SettlementResponseDto cancelSettlement(
            String settlementPublicId,
            String actorOrganizationPublicId,
            String cancelledByUserPublicId,
            String userRole
    ) {
        validateSettlementActionHeader(actorOrganizationPublicId, cancelledByUserPublicId, userRole);

        Settlement settlement = settlementRepository.findReadableByPublicId(
                        settlementPublicId,
                        actorOrganizationPublicId
                )
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.SETTLEMENT_NOT_FOUND));

        if (!actorOrganizationPublicId.equals(settlement.getBuyerOrganizationPublicId())) {
            throw new SettlementException(SettlementErrorCode.FORBIDDEN_SETTLEMENT_CANCEL);
        }

        try {
            settlement.cancel(cancelledByUserPublicId);
        } catch (IllegalStateException e) {
            throw new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_STATUS_TRANSITION);
        }

        List<SettlementDetail> details =
                settlementDetailRepository.findAllBySettlement_IdOrderByIdAsc(settlement.getId());

        details.forEach(SettlementDetail::cancel);

        settlementSearchService.saveSettlementDocument(settlement);

        String supplierPublicId = getSupplierPublicId(settlement.getSupplierId());

        return toResponseDto(settlement, supplierPublicId, details);
    }

    // 기존 정산 조회/응답 변환 시 저장된 supplierId 기준으로 협력사 조회
    private String getSupplierPublicId(Long supplierId) {
        return supplierRepository.findById(supplierId)
                .map(SupplySupplier::getPublicId)
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.SUPPLIER_NOT_FOUND));
    }

    // 정산 헤더와 상세 목록을 포함한 상세 응답 DTO 변환
    private SettlementResponseDto toResponseDto(
            Settlement settlement,
            String supplierPublicId,
            List<SettlementDetail> details
    ) {
        return SettlementResponseDto.builder()
                .id(settlement.getId())
                .publicId(settlement.getPublicId())
                .buyerOrganizationPublicId(settlement.getBuyerOrganizationPublicId())
                .supplierOrganizationPublicId(settlement.getSupplierOrganizationPublicId())
                .supplierPublicId(supplierPublicId)
                .targetType(settlement.getTargetType())
                .targetPublicId(settlement.getTargetPublicId())
                .settlementPeriodStart(settlement.getSettlementPeriodStart())
                .settlementPeriodEnd(settlement.getSettlementPeriodEnd())
                .amount(settlement.getAmount())
                .currencyCode(settlement.getCurrencyCode())
                .settlementStatus(settlement.getSettlementStatus())
                .settledAt(settlement.getSettledAt())
                .approvedByUserPublicId(settlement.getApprovedByUserPublicId())
                .cancelledAt(settlement.getCancelledAt())
                .cancelledByUserPublicId(settlement.getCancelledByUserPublicId())
                .createdAt(settlement.getCreatedAt())
                .updatedAt(settlement.getUpdatedAt())
                .details(details.stream()
                        .map(this::toDetailResponseDto)
                        .toList())
                .build();
    }

    // 정산 목록 조회용 응답 DTO 변환
    private SettlementResponseDto toResponseDtoWithoutDetails(Settlement settlement) {
        String supplierPublicId = getSupplierPublicId(settlement.getSupplierId());

        return SettlementResponseDto.builder()
                .id(settlement.getId())
                .publicId(settlement.getPublicId())
                .buyerOrganizationPublicId(settlement.getBuyerOrganizationPublicId())
                .supplierOrganizationPublicId(settlement.getSupplierOrganizationPublicId())
                .supplierPublicId(supplierPublicId)
                .targetType(settlement.getTargetType())
                .targetPublicId(settlement.getTargetPublicId())
                .settlementPeriodStart(settlement.getSettlementPeriodStart())
                .settlementPeriodEnd(settlement.getSettlementPeriodEnd())
                .amount(settlement.getAmount())
                .currencyCode(settlement.getCurrencyCode())
                .settlementStatus(settlement.getSettlementStatus())
                .settledAt(settlement.getSettledAt())
                .approvedByUserPublicId(settlement.getApprovedByUserPublicId())
                .cancelledAt(settlement.getCancelledAt())
                .cancelledByUserPublicId(settlement.getCancelledByUserPublicId())
                .createdAt(settlement.getCreatedAt())
                .updatedAt(settlement.getUpdatedAt())
                .details(List.of())
                .build();
    }

    // 정산 상세 엔티티를 응답 DTO 변환
    private SettlementDetailResponseDto toDetailResponseDto(SettlementDetail detail) {
        return SettlementDetailResponseDto.builder()
                .publicId(detail.getPublicId())
                .poItemId(detail.getPoItemId())
                .itemId(detail.getItemId())
                .qty(detail.getQty())
                .unitPrice(detail.getUnitPrice())
                .amount(detail.getAmount())
                .detailStatus(detail.getDetailStatus())
                .build();
    }

    private static final String ADMIN_ROLE = "ADMIN";

//    반품-정산 요청 검증
    private void validateCreateRequest(CreateSettlementRequestDto request) {
        if (request.getTargetType() != SettlementTargetType.ORDER
                && request.getTargetType() != SettlementTargetType.SHIPMENT
                && request.getTargetType() != SettlementTargetType.RETURN) {
            throw new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_REQUEST);
        }

        if (settlementRepository.existsByTargetTypeAndTargetPublicIdAndSettlementStatusNot(
                request.getTargetType(),
                request.getTargetPublicId(),
                SettlementStatus.CANCELLED
        )) {
            throw new SettlementException(SettlementErrorCode.DUPLICATE_SETTLEMENT_TARGET);
        }
    }

    private SettlementContext resolveSettlementContext(
            CreateSettlementRequestDto request,
            String actorOrganizationPublicId
    ) {
        if (request.getTargetType() == SettlementTargetType.ORDER) {
            return resolveOrderSettlementContext(request.getTargetPublicId(), actorOrganizationPublicId);
        }

        if (request.getTargetType() == SettlementTargetType.SHIPMENT) {
            return resolveShipmentSettlementContext(request.getTargetPublicId(), actorOrganizationPublicId);
        }

        if (request.getTargetType() == SettlementTargetType.RETURN) {
            return resolveReturnSettlementContext(request.getTargetPublicId(), actorOrganizationPublicId);
        }

        throw new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_REQUEST);
    }

    private SettlementContext resolveShipmentSettlementContext(
            String shipmentPublicId,
            String actorOrganizationPublicId
    ) {
        Shipment shipment = getShipmentByPublicId(shipmentPublicId);

        if (shipment.getStatus() != ShipmentStatus.ARRIVED) {
            throw new SettlementException(SettlementErrorCode.SHIPMENT_NOT_SETTLABLE);
        }

        LogisticsNode originNode = getLogisticsNodeById(shipment.getOriginNodeId());
        LogisticsNode destinationNode = getLogisticsNodeById(shipment.getDestinationNodeId());

        String supplierOrganizationPublicId = originNode.getOrganizationPublicId();
        String buyerOrganizationPublicId = destinationNode.getOrganizationPublicId();

        if (!actorOrganizationPublicId.equals(buyerOrganizationPublicId)) {
            throw new SettlementException(SettlementErrorCode.FORBIDDEN_SETTLEMENT_CREATE);
        }

        SupplySupplier supplier = getSettlementSupplierByOrganizationPublicId(supplierOrganizationPublicId);

        return new SettlementContext(
                supplier,
                buyerOrganizationPublicId,
                supplierOrganizationPublicId
        );
    }

    private SettlementContext resolveOrderSettlementContext(
            String orderPublicId,
            String actorOrganizationPublicId
    ) {
        SupplyPurchaseOrder purchaseOrder = purchaseOrderRepository.findByPublicIdAndPoStatusNot(
                        orderPublicId,
                        PoStatus.DELETED
                )
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_REQUEST));

        if (!actorOrganizationPublicId.equals(purchaseOrder.getBuyerOrganizationPublicId())) {
            throw new SettlementException(SettlementErrorCode.FORBIDDEN_SETTLEMENT_CREATE);
        }

        SupplySupplier supplier = getSettlementSupplierByOrganizationPublicId(
                purchaseOrder.getSupplier().getOrganizationPublicId()
        );

        return new SettlementContext(
                supplier,
                purchaseOrder.getBuyerOrganizationPublicId(),
                purchaseOrder.getSupplier().getOrganizationPublicId()
        );
    }

    private SettlementContext resolveReturnSettlementContext(
            String returnPublicId,
            String actorOrganizationPublicId
    ) {
        ReturnRequest returnRequest = returnRequestRepository.findByPublicId(returnPublicId)
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.RETURN_NOT_FOUND));

        if (returnRequest.getReturnStatus() != ReturnStatus.COMPLETED) {
            throw new SettlementException(SettlementErrorCode.RETURN_NOT_SETTLABLE);
        }

        if (!actorOrganizationPublicId.equals(returnRequest.getRequestOrganizationPublicId())) {
            throw new SettlementException(SettlementErrorCode.FORBIDDEN_SETTLEMENT_CREATE);
        }

        SupplySupplier supplier = getSettlementSupplierByOrganizationPublicId(
                returnRequest.getTargetOrganizationPublicId()
        );

        return new SettlementContext(
                supplier,
                returnRequest.getRequestOrganizationPublicId(),
                returnRequest.getTargetOrganizationPublicId()
        );
    }

    private List<SettlementDetail> createShipmentSettlementDetails(
            Settlement settlement,
            String shipmentPublicId
    ) {
        Shipment shipment = getShipmentByPublicId(shipmentPublicId);

        if (shipment.getPoId() != null) {
            return createPurchaseOrderSettlementDetails(settlement, shipment.getPoId());
        }

        if (shipment.getSubPoId() != null) {
            return createSubPurchaseOrderSettlementDetails(settlement, shipment.getSubPoId());
        }

        throw new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_REQUEST);
    }

    private List<SettlementDetail> createOrderSettlementDetails(
            Settlement settlement,
            String orderPublicId
    ) {
        SupplyPurchaseOrder purchaseOrder = purchaseOrderRepository.findByPublicIdAndPoStatusNot(
                        orderPublicId,
                        PoStatus.DELETED
                )
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_REQUEST));

        return createPurchaseOrderSettlementDetails(settlement, purchaseOrder.getId());
    }

    private List<SettlementDetail> createPurchaseOrderSettlementDetails(
            Settlement settlement,
            Long poId
    ) {
        SupplyPurchaseOrder purchaseOrder = purchaseOrderRepository.findById(poId)
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_REQUEST));

        List<SettlementDetail> details = purchaseOrder.getActiveItems().stream()
                .map(item -> toPurchaseOrderSettlementDetail(settlement, item))
                .toList();

        if (details.isEmpty()) {
            throw new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_REQUEST);
        }

        return details;
    }

    private List<SettlementDetail> createSubPurchaseOrderSettlementDetails(
            Settlement settlement,
            Long subPoId
    ) {
        SupplySubPurchaseOrder subPurchaseOrder = subPurchaseOrderRepository.findById(subPoId)
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_REQUEST));

        List<SettlementDetail> details = subPurchaseOrder.getActiveItems().stream()
                .map(item -> toSubPurchaseOrderSettlementDetail(settlement, item))
                .toList();

        if (details.isEmpty()) {
            throw new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_REQUEST);
        }

        return details;
    }

    private SettlementDetail toPurchaseOrderSettlementDetail(
            Settlement settlement,
            SupplyPurchaseOrderItem item
    ) {
        BigDecimal qty = toSettlementQty(item.getConfirmedQty(), item.getOrderedQty());
        BigDecimal unitPrice = item.getUnitPrice();
        BigDecimal amount = qty.multiply(unitPrice);

        return SettlementDetail.builder()
                .settlement(settlement)
                .poItemId(item.getPoItemId())
                .itemId(item.getItem().getId())
                .qty(qty)
                .unitPrice(unitPrice)
                .amount(amount)
                .build();
    }

    private SettlementDetail toSubPurchaseOrderSettlementDetail(
            Settlement settlement,
            SupplySubPurchaseOrderItem item
    ) {
        BigDecimal qty = toSettlementQty(item.getConfirmedQty(), item.getOrderedQty());
        BigDecimal unitPrice = item.getUnitPrice();
        BigDecimal amount = qty.multiply(unitPrice);

        return SettlementDetail.builder()
                .settlement(settlement)
                .poItemId(item.getParentPurchaseOrderItem().getPoItemId())
                .itemId(item.getItem().getId())
                .qty(qty)
                .unitPrice(unitPrice)
                .amount(amount)
                .build();
    }

    private BigDecimal toSettlementQty(Long confirmedQty, Long orderedQty) {
        Long qty = confirmedQty != null ? confirmedQty : orderedQty;

        if (qty == null || qty <= 0) {
            throw new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_REQUEST);
        }

        return BigDecimal.valueOf(qty);
    }

    private void createPurchaseOrderSettlementIfReady(Shipment arrivedShipment) {
        String orderPublicId = arrivedShipment.getPurchaseOrderPublicId();

        if (settlementRepository.existsByTargetTypeAndTargetPublicIdAndSettlementStatusNot(
                SettlementTargetType.ORDER,
                orderPublicId,
                SettlementStatus.CANCELLED
        )) {
            return;
        }

        SupplyPurchaseOrder purchaseOrder = purchaseOrderRepository.findById(arrivedShipment.getPoId())
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_REQUEST));

        if (!isPurchaseOrderFullyArrived(purchaseOrder)) {
            return;
        }

        createSettlement(
                CreateSettlementRequestDto.builder()
                        .targetType(SettlementTargetType.ORDER)
                        .targetPublicId(orderPublicId)
                        .currencyCode(toSettlementCurrency(purchaseOrder.getCurrencyCode().name()))
                        .build(),
                purchaseOrder.getBuyerOrganizationPublicId(),
                "SYSTEM"
        );
    }

    private boolean isPurchaseOrderFullyArrived(SupplyPurchaseOrder purchaseOrder) {
        List<Shipment> shipments = shipmentRepository.findByPoId(purchaseOrder.getId()).stream()
                .filter(shipment -> shipment.getStatus() != ShipmentStatus.CANCELLED)
                .toList();

        if (shipments.isEmpty() || shipments.stream().anyMatch(shipment -> shipment.getStatus() != ShipmentStatus.ARRIVED)) {
            return false;
        }

        return purchaseOrder.getActiveItems().stream()
                .allMatch(item -> {
                    Long confirmedQty = item.getConfirmedQty();
                    if (confirmedQty == null || confirmedQty <= 0) {
                        return true;
                    }

                    Long arrivedQty = shipmentLineRepository.sumQuantityBySourceItemPublicIdAndShipmentStatusIn(
                            item.getPublicId(),
                            List.of(ShipmentStatus.ARRIVED)
                    );

                    return arrivedQty != null && arrivedQty >= confirmedQty;
                });
    }

    private Shipment getShipmentByPublicId(String shipmentPublicId) {
        return shipmentRepository.findByPublicId(shipmentPublicId)
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.SHIPMENT_NOT_FOUND));
    }

    private SettlementCurrency resolveShipmentCurrencyCode(Shipment shipment) {
        if (shipment.getPoId() != null) {
            SupplyPurchaseOrder purchaseOrder = purchaseOrderRepository.findById(shipment.getPoId())
                    .orElseThrow(() -> new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_REQUEST));
            return toSettlementCurrency(purchaseOrder.getCurrencyCode().name());
        }

        if (shipment.getSubPoId() != null) {
            return SettlementCurrency.KRW;
        }

        return SettlementCurrency.KRW;
    }

    private SettlementCurrency resolveReturnCurrencyCode(ReturnRequest returnRequest) {
        Shipment shipment = shipmentRepository.findByPublicId(returnRequest.getSourceShipmentPublicId())
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_REQUEST));

        return resolveShipmentCurrencyCode(shipment);
    }

    private SettlementCurrency toSettlementCurrency(String currencyCode) {
        if ("USD".equalsIgnoreCase(currencyCode) || "DOLLAR".equalsIgnoreCase(currencyCode)) {
            return SettlementCurrency.DOLLAR;
        }

        return SettlementCurrency.KRW;
    }

    private LogisticsNode getLogisticsNodeById(Long nodeId) {
        return logisticsNodeRepository.findById(nodeId)
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_REQUEST));
    }

    private SupplySupplier getSettlementSupplierByOrganizationPublicId(String organizationPublicId) {
        SupplySupplier supplier = supplierRepository.findByOrganizationPublicId(organizationPublicId)
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.SUPPLIER_NOT_FOUND));

        if (supplier.getSupplierStatus() == SupplierStatus.TERMINATED) {
            throw new SettlementException(SettlementErrorCode.SUPPLIER_NOT_FOUND);
        }

        return supplier;
    }

    private void validateSettlementActorHeader(String actorOrganizationPublicId, String userRole) {
        if (actorOrganizationPublicId == null || actorOrganizationPublicId.isBlank()
                || userRole == null || userRole.isBlank()) {
            throw new SettlementException(SettlementErrorCode.INVALID_ACTOR_HEADER);
        }

        if (ADMIN_ROLE.equals(userRole)) {
            throw new SettlementException(SettlementErrorCode.FORBIDDEN_SETTLEMENT_ACCESS);
        }
    }

    private void validateSettlementActionHeader(
            String actorOrganizationPublicId,
            String actorUserPublicId,
            String userRole
    ) {
        validateSettlementActorHeader(actorOrganizationPublicId, userRole);

        if (actorUserPublicId == null || actorUserPublicId.isBlank()) {
            throw new SettlementException(SettlementErrorCode.INVALID_ACTOR_HEADER);
        }
    }

    private record SettlementContext(
            SupplySupplier supplier,
            String buyerOrganizationPublicId,
            String supplierOrganizationPublicId
    ) {
    }

    //    반품 정산 상세 생성
    private List<SettlementDetail> createReturnSettlementDetails(
            Settlement settlement,
            CreateSettlementRequestDto request
    ) {
        ReturnRequest returnRequest = returnRequestRepository.findByPublicId(request.getTargetPublicId())
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.RETURN_NOT_FOUND));

        if (returnRequest.getReturnStatus() != ReturnStatus.COMPLETED) {
            throw new SettlementException(SettlementErrorCode.RETURN_NOT_SETTLABLE);
        }

        Shipment shipment = shipmentRepository.findByPublicId(returnRequest.getSourceShipmentPublicId())
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_REQUEST));

        if (shipment.getPoId() != null) {
            return createPurchaseOrderReturnSettlementDetails(settlement, shipment.getPoId(), returnRequest);
        }

        if (shipment.getSubPoId() != null) {
            return createSubPurchaseOrderReturnSettlementDetails(settlement, shipment.getSubPoId(), returnRequest);
        }

        throw new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_REQUEST);
    }

    private List<SettlementDetail> createPurchaseOrderReturnSettlementDetails(
            Settlement settlement,
            Long poId,
            ReturnRequest returnRequest
    ) {
        SupplyPurchaseOrder purchaseOrder = purchaseOrderRepository.findById(poId)
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_REQUEST));

        if (!purchaseOrder.getSupplier().getId().equals(settlement.getSupplierId())) {
            throw new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_REQUEST);
        }

        List<SettlementDetail> details = returnRequest.getItems().stream()
                .map(returnItem -> toPurchaseOrderReturnSettlementDetail(settlement, purchaseOrder, returnItem))
                .toList();

        if (details.isEmpty()) {
            throw new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_REQUEST);
        }

        return details;
    }

    private List<SettlementDetail> createSubPurchaseOrderReturnSettlementDetails(
            Settlement settlement,
            Long subPoId,
            ReturnRequest returnRequest
    ) {
        SupplySubPurchaseOrder subPurchaseOrder = subPurchaseOrderRepository.findById(subPoId)
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_REQUEST));

        if (!subPurchaseOrder.getSupplier().getId().equals(settlement.getSupplierId())) {
            throw new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_REQUEST);
        }

        List<SettlementDetail> details = returnRequest.getItems().stream()
                .map(returnItem -> toSubPurchaseOrderReturnSettlementDetail(settlement, subPurchaseOrder, returnItem))
                .toList();

        if (details.isEmpty()) {
            throw new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_REQUEST);
        }

        return details;
    }

    //    반품 item 1건을 정산 detail로 바꾸는 메서드
    private SettlementDetail toPurchaseOrderReturnSettlementDetail(
            Settlement settlement,
            SupplyPurchaseOrder purchaseOrder,
            ReturnItem returnItem
    ) {
        SupplyPurchaseOrderItem poItem = purchaseOrder.getPurchaseOrderItems().stream()
                .filter(item -> item.getItem().getPublicId().equals(returnItem.getItemPublicId()))
                .findFirst()
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_REQUEST));

        BigDecimal qty = returnItem.getReturnQty().negate();
        BigDecimal unitPrice = poItem.getUnitPrice();
        BigDecimal amount = qty.multiply(unitPrice);

        return SettlementDetail.builder()
                .settlement(settlement)
                .poItemId(poItem.getPoItemId())
                .itemId(poItem.getItem().getId())
                .qty(qty)
                .unitPrice(unitPrice)
                .amount(amount)
                .build();
    }

    private SettlementDetail toSubPurchaseOrderReturnSettlementDetail(
            Settlement settlement,
            SupplySubPurchaseOrder subPurchaseOrder,
            ReturnItem returnItem
    ) {
        SupplySubPurchaseOrderItem subPoItem = subPurchaseOrder.getSubPurchaseOrderItems().stream()
                .filter(item -> item.getItem().getPublicId().equals(returnItem.getItemPublicId()))
                .findFirst()
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_REQUEST));

        BigDecimal qty = returnItem.getReturnQty().negate();
        BigDecimal unitPrice = subPoItem.getUnitPrice();
        BigDecimal amount = qty.multiply(unitPrice);

        return SettlementDetail.builder()
                .settlement(settlement)
                .poItemId(subPoItem.getParentPurchaseOrderItem().getPoItemId())
                .itemId(subPoItem.getItem().getId())
                .qty(qty)
                .unitPrice(unitPrice)
                .amount(amount)
                .build();
    }

    // 정산 통계 조회
    @Transactional(readOnly = true)
    public SettlementStatisticsResponseDto getSettlementStatistics(
            Integer year,
            String actorOrganizationPublicId,
            String userRole
    ) {
        // 정산 목록 조회와 같은 권한 규칙을 사용
        validateSettlementActorHeader(actorOrganizationPublicId, userRole);

        // year 값이 없으면 현재 연도를 기준으로 조회
        int targetYear = year != null ? year : LocalDate.now().getYear();

        // 이번 달 카드 계산에 사용할 현재 월
        int currentMonth = LocalDate.now().getMonthValue();

        // 기존 통계
        List<SettlementStatisticsResponseDto.ChartPointDto> yearlyAmounts =
                toYearlyChartPoints(
                        settlementRepository.findYearlyAmountStats(actorOrganizationPublicId)
                );

        List<SettlementStatisticsResponseDto.ChartPointDto> monthlyAmounts =
                toMonthlyChartPoints(
                        settlementRepository.findMonthlyAmountStats(
                                targetYear,
                                actorOrganizationPublicId
                        )
                );

        List<SettlementStatisticsResponseDto.ChartPointDto> statusAmounts =
                toStatusChartPoints(
                        settlementRepository.findStatusAmountStats(
                                targetYear,
                                actorOrganizationPublicId
                        )
                );

        List<SettlementStatisticsResponseDto.ChartPointDto> targetTypeAmounts =
                toTargetTypeChartPoints(
                        settlementRepository.findTargetTypeAmountStats(
                                targetYear,
                                actorOrganizationPublicId
                        )
                );

        // 예산 계산용 통계
        Map<Integer, BigDecimal> payableAmountMap = toMonthlyAmountMap(
                settlementRepository.findMonthlyPayableAmountStats(
                        targetYear,
                        actorOrganizationPublicId
                )
        );

        Map<Integer, BigDecimal> receivableAmountMap = toMonthlyAmountMap(
                settlementRepository.findMonthlyReceivableAmountStats(
                        targetYear,
                        actorOrganizationPublicId
                )
        );

        // 월별 예산 대비 지급 정산액 데이터
        List<SettlementBudgetUsageDto> monthlyBudgetUsages =
                buildMonthlyBudgetUsages(
                        targetYear,
                        actorOrganizationPublicId
                );

        SettlementBudgetUsageDto currentMonthBudgetUsage = monthlyBudgetUsages.stream()
                .filter(item -> item.getMonth() == currentMonth)
                .findFirst()
                .orElse(null);

        Map<String, SettlementStatisticsResponseDto.ChartPointDto> statusAmountMap =
                statusAmounts.stream()
                        .collect(Collectors.toMap(
                                SettlementStatisticsResponseDto.ChartPointDto::getValue,
                                point -> point
                        ));

        // 올해 전체 정산 총액
        BigDecimal totalAmountThisYear = statusAmounts.stream()
                .map(SettlementStatisticsResponseDto.ChartPointDto::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 올해 전체 정산 건수
        Long totalCount = statusAmounts.stream()
                .map(SettlementStatisticsResponseDto.ChartPointDto::getCount)
                .reduce(0L, Long::sum);

        // 이번 달 전체 정산 총액
        BigDecimal totalAmountThisMonth = monthlyAmounts.stream()
                .filter(point -> String.valueOf(currentMonth).equals(point.getValue()))
                .map(SettlementStatisticsResponseDto.ChartPointDto::getAmount)
                .findFirst()
                .orElse(BigDecimal.ZERO);

        // 올해 지급 정산 총액
        BigDecimal payableAmountThisYear = payableAmountMap.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 올해 수금 예정 총액
        BigDecimal receivableAmountThisYear = receivableAmountMap.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 이번 달 지급 정산액
        BigDecimal payableAmountThisMonth =
                payableAmountMap.getOrDefault(currentMonth, BigDecimal.ZERO);

        // 이번 달 수금 예정액
        BigDecimal receivableAmountThisMonth =
                receivableAmountMap.getOrDefault(currentMonth, BigDecimal.ZERO);

        SettlementStatisticsResponseDto.ChartPointDto pendingPoint =
                statusAmountMap.get(SettlementStatus.PENDING.name());

        SettlementStatisticsResponseDto.ChartPointDto approvedPoint =
                statusAmountMap.get(SettlementStatus.APPROVED.name());

        SettlementStatisticsResponseDto.ChartPointDto cancelledPoint =
                statusAmountMap.get(SettlementStatus.CANCELLED.name());

        // 상태별 금액
        BigDecimal pendingAmount = getChartAmount(pendingPoint);
        BigDecimal approvedAmount = getChartAmount(approvedPoint);
        BigDecimal cancelledAmount = getChartAmount(cancelledPoint);

        // 상태별 건수
        Long pendingCount = getChartCount(pendingPoint);
        Long approvedCount = getChartCount(approvedPoint);
        Long cancelledCount = getChartCount(cancelledPoint);

        // 승인률
        BigDecimal approvalRate = totalCount == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(approvedCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalCount), 1, RoundingMode.HALF_UP);

        // 이번 달 예산 정보
        BigDecimal currentMonthBudgetAmount = currentMonthBudgetUsage == null
                ? BigDecimal.ZERO
                : currentMonthBudgetUsage.getBudgetAmount();

        BigDecimal currentMonthRemainingBudgetAmount = currentMonthBudgetUsage == null
                ? BigDecimal.ZERO
                : currentMonthBudgetUsage.getRemainingAmount();

        BigDecimal currentMonthBudgetUsageRate = currentMonthBudgetUsage == null
                ? BigDecimal.ZERO
                : currentMonthBudgetUsage.getUsageRate();

        BudgetUsageStatus currentMonthBudgetStatus = currentMonthBudgetUsage == null
                ? BudgetUsageStatus.NO_BUDGET
                : currentMonthBudgetUsage.getStatus();
        // 현재 조직과 관련된 전체 발주 건수
        long purchaseOrderCount = purchaseOrderRepository.countRelatedPurchaseOrders(
                actorOrganizationPublicId,
                PoStatus.DELETED
        );

// 아직 처리 중인 발주 건수
        long pendingPurchaseOrderCount = purchaseOrderRepository.countRelatedPurchaseOrdersByStatuses(
                actorOrganizationPublicId,
                List.of(PoStatus.CREATED, PoStatus.PARTIALLY_CONFIRMED)
        );

        List<Long> organizationNodeIds = logisticsNodeRepository.findByOrganizationPublicId(actorOrganizationPublicId)
                .stream()
                .map(LogisticsNode::getId)
                .toList();

// 배송중 건수
        long inTransitShipmentCount = countRelatedShipmentsByStatuses(
                organizationNodeIds,
                List.of(ShipmentStatus.IN_TRANSIT)
        );

// 배송 지연 건수
        long delayedShipmentCount = countRelatedShipmentsByStatuses(
                organizationNodeIds,
                List.of(ShipmentStatus.DELAYED)
        );

// 완료/반려 전까지의 반품 진행 건수
        long returnInProgressCount =
                returnRequestRepository.countByReturnStatusInAndRequestOrganizationPublicIdOrReturnStatusInAndTargetOrganizationPublicId(
                        List.of(
                                ReturnStatus.REQUESTED,
                                ReturnStatus.APPROVED,
                                ReturnStatus.IN_TRANSIT,
                                ReturnStatus.RECEIVED,
                                ReturnStatus.RESHIPPED,
                                ReturnStatus.DISPOSED
                        ),
                        actorOrganizationPublicId,
                        List.of(
                                ReturnStatus.REQUESTED,
                                ReturnStatus.APPROVED,
                                ReturnStatus.IN_TRANSIT,
                                ReturnStatus.RECEIVED,
                                ReturnStatus.RESHIPPED,
                                ReturnStatus.DISPOSED
                        ),
                        actorOrganizationPublicId
                );

// 프론트 ApexCharts 운영 상태 막대 차트용 데이터
        List<SettlementStatisticsResponseDto.ChartPointDto> operationStatusCounts = List.of(
                buildOperationCountPoint("발주 전체", "PURCHASE_ORDER", purchaseOrderCount),
                buildOperationCountPoint("발주 대기", "PENDING_PURCHASE_ORDER", pendingPurchaseOrderCount),
                buildOperationCountPoint("배송중", "IN_TRANSIT_SHIPMENT", inTransitShipmentCount),
                buildOperationCountPoint("배송지연", "DELAYED_SHIPMENT", delayedShipmentCount),
                buildOperationCountPoint("반품진행", "RETURN_IN_PROGRESS", returnInProgressCount)
        );


        return SettlementStatisticsResponseDto.builder()
                .year(targetYear)

                // 기존 전체 정산 통계
                .totalAmountThisYear(totalAmountThisYear)
                .totalAmountThisMonth(totalAmountThisMonth)
                .pendingAmount(pendingAmount)
                .approvedAmount(approvedAmount)
                .cancelledAmount(cancelledAmount)
                .totalCount(totalCount)
                .pendingCount(pendingCount)
                .approvedCount(approvedCount)
                .cancelledCount(cancelledCount)
                .approvalRate(approvalRate)

                // 지급/수금 분리 통계
                .payableAmountThisYear(payableAmountThisYear)
                .payableAmountThisMonth(payableAmountThisMonth)
                .receivableAmountThisYear(receivableAmountThisYear)
                .receivableAmountThisMonth(receivableAmountThisMonth)

                // 이번 달 예산 요약
                .currentMonthBudgetAmount(currentMonthBudgetAmount)
                .currentMonthRemainingBudgetAmount(currentMonthRemainingBudgetAmount)
                .currentMonthBudgetUsageRate(currentMonthBudgetUsageRate)
                .currentMonthBudgetStatus(currentMonthBudgetStatus)

                // 차트 데이터
                .yearlyAmounts(yearlyAmounts)
                .monthlyAmounts(monthlyAmounts)
                .statusAmounts(statusAmounts)
                .targetTypeAmounts(targetTypeAmounts)
                .monthlyBudgetUsages(monthlyBudgetUsages)
                .purchaseOrderCount(purchaseOrderCount)
                .pendingPurchaseOrderCount(pendingPurchaseOrderCount)
                .inTransitShipmentCount(inTransitShipmentCount)
                .delayedShipmentCount(delayedShipmentCount)
                .returnInProgressCount(returnInProgressCount)
                .operationStatusCounts(operationStatusCounts)

                .build();
    }



    private List<SettlementStatisticsResponseDto.ChartPointDto> toYearlyChartPoints(List<Object[]> rows) {
        return rows.stream()
                .filter(row -> row[0] != null)
                .map(row -> {
                    Integer year = ((Number) row[0]).intValue();

                    return SettlementStatisticsResponseDto.ChartPointDto.builder()
                            .label(year + "년")
                            .value(String.valueOf(year))
                            .amount(toBigDecimal(row[1]))
                            .count(toLong(row[2]))
                            .build();
                })
                .toList();
    }

    private List<SettlementStatisticsResponseDto.ChartPointDto> toMonthlyChartPoints(List<Object[]> rows) {
        return rows.stream()
                .filter(row -> row[0] != null)
                .map(row -> {
                    Integer month = ((Number) row[0]).intValue();

                    return SettlementStatisticsResponseDto.ChartPointDto.builder()
                            .label(month + "월")
                            .value(String.valueOf(month))
                            .amount(toBigDecimal(row[1]))
                            .count(toLong(row[2]))
                            .build();
                })
                .toList();
    }

    private List<SettlementStatisticsResponseDto.ChartPointDto> toStatusChartPoints(List<Object[]> rows) {
        return rows.stream()
                .filter(row -> row[0] != null)
                .map(row -> {
                    SettlementStatus status = (SettlementStatus) row[0];

                    return SettlementStatisticsResponseDto.ChartPointDto.builder()
                            .label(formatSettlementStatusLabel(status))
                            .value(status.name())
                            .amount(toBigDecimal(row[1]))
                            .count(toLong(row[2]))
                            .build();
                })
                .toList();
    }

    private List<SettlementStatisticsResponseDto.ChartPointDto> toTargetTypeChartPoints(List<Object[]> rows) {
        return rows.stream()
                .filter(row -> row[0] != null)
                .map(row -> {
                    SettlementTargetType targetType = (SettlementTargetType) row[0];

                    return SettlementStatisticsResponseDto.ChartPointDto.builder()
                            .label(formatSettlementTargetTypeLabel(targetType))
                            .value(targetType.name())
                            .amount(toBigDecimal(row[1]))
                            .count(toLong(row[2]))
                            .build();
                })
                .toList();
    }
    // 월별 예산을 저장하거나 수정
    @Transactional
    public SettlementBudgetResponseDto saveSettlementBudget(
            SettlementBudgetRequestDto request,
            String actorOrganizationPublicId,
            String updatedByUserPublicId,
            String userRole
    ) {
        // 기존 정산 권한 검증 규칙을 그대로 사용
        validateSettlementActorHeader(actorOrganizationPublicId, userRole);

        BigDecimal warningThresholdRate = request.getWarningThresholdRate() == null
                ? BigDecimal.valueOf(80)
                : request.getWarningThresholdRate();

        SettlementBudget budget = settlementBudgetRepository
                .findByOrganizationPublicIdAndYearAndMonthAndCurrencyCode(
                        actorOrganizationPublicId,
                        request.getYear(),
                        request.getMonth(),
                        request.getCurrencyCode()
                )
                .orElseGet(() -> SettlementBudget.builder()
                        .organizationPublicId(actorOrganizationPublicId)
                        .year(request.getYear())
                        .month(request.getMonth())
                        .currencyCode(request.getCurrencyCode())
                        .budgetAmount(BigDecimal.ZERO)
                        .warningThresholdRate(warningThresholdRate)
                        .updatedByUserPublicId(updatedByUserPublicId)
                        .build());

        budget.update(
                request.getBudgetAmount(),
                warningThresholdRate,
                updatedByUserPublicId
        );

        return SettlementBudgetResponseDto.fromEntity(
                settlementBudgetRepository.save(budget)
        );
    }


    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }

        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }

        return new BigDecimal(value.toString());
    }

    private Long toLong(Object value) {
        if (value == null) {
            return 0L;
        }

        return ((Number) value).longValue();
    }

    private BigDecimal getChartAmount(SettlementStatisticsResponseDto.ChartPointDto point) {
        return point == null ? BigDecimal.ZERO : point.getAmount();
    }

    private Long getChartCount(SettlementStatisticsResponseDto.ChartPointDto point) {
        return point == null ? 0L : point.getCount();
    }

    // 정산 상태 코드를 화면용 한글 라벨로
    private String formatSettlementStatusLabel(SettlementStatus status) {
        if (status == SettlementStatus.APPROVED) {
            return "승인 완료";
        }

        if (status == SettlementStatus.CANCELLED) {
            return "취소";
        }

        return "대기";
    }

    // 정산 대상 유형 코드를 화면용 한글 라벨로
    private String formatSettlementTargetTypeLabel(SettlementTargetType targetType) {
        if (targetType == SettlementTargetType.ORDER) {
            return "발주";
        }

        if (targetType == SettlementTargetType.RETURN) {
            return "반품";
        }

        if (targetType == SettlementTargetType.DELIVERY_EXCEPTION) {
            return "배송 예외";
        }

        return "출하";
    }


    private Map<Integer, BigDecimal> toMonthlyAmountMap(List<Object[]> rows) {
        Map<Integer, BigDecimal> result = new HashMap<>();

        for (Object[] row : rows) {
            if (row[0] == null) {
                continue;
            }

            result.put(
                    ((Number) row[0]).intValue(),
                    toBigDecimal(row[1])
            );
        }

        return result;
    }

    private Map<Integer, SettlementBudget> toBudgetMap(List<SettlementBudget> budgets) {
        Map<Integer, SettlementBudget> result = new HashMap<>();

        for (SettlementBudget budget : budgets) {
            result.put(budget.getMonth(), budget);
        }

        return result;
    }

    // 월별 예산 대비 지급 정산액 데이터를 만듬
    private List<SettlementBudgetUsageDto> buildMonthlyBudgetUsages(
            Integer year,
            String organizationPublicId
    ) {
        Map<Integer, BigDecimal> payableAmountMap = toMonthlyAmountMap(
                settlementRepository.findMonthlyPayableAmountStats(year, organizationPublicId)
        );

        Map<Integer, SettlementBudget> budgetMap = toBudgetMap(
                settlementBudgetRepository.findAllByOrganizationPublicIdAndYear(
                        organizationPublicId,
                        year
                )
        );

        List<SettlementBudgetUsageDto> result = new ArrayList<>();

        for (int month = 1; month <= 12; month++) {
            SettlementBudget budget = budgetMap.get(month);
            BigDecimal budgetAmount = budget == null ? BigDecimal.ZERO : budget.getBudgetAmount();
            BigDecimal payableAmount = payableAmountMap.getOrDefault(month, BigDecimal.ZERO);

            BigDecimal remainingAmount = budgetAmount.subtract(payableAmount);
            BigDecimal usageRate = calculateBudgetUsageRate(payableAmount, budgetAmount);
            BudgetUsageStatus status = resolveBudgetUsageStatus(budget, payableAmount, usageRate);

            result.add(
                    SettlementBudgetUsageDto.builder()
                            .month(month)
                            .label(month + "월")
                            .budgetAmount(budgetAmount)
                            .payableAmount(payableAmount)
                            .remainingAmount(remainingAmount)
                            .usageRate(usageRate)
                            .status(status)
                            .build()
            );
        }

        return result;
    }

    // 예산 사용률을 계산
    private BigDecimal calculateBudgetUsageRate(
            BigDecimal payableAmount,
            BigDecimal budgetAmount
    ) {
        if (budgetAmount == null || budgetAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        return payableAmount
                .multiply(BigDecimal.valueOf(100))
                .divide(budgetAmount, 2, RoundingMode.HALF_UP);
    }

    // 예산 상태를 계산
    private BudgetUsageStatus resolveBudgetUsageStatus(
            SettlementBudget budget,
            BigDecimal payableAmount,
            BigDecimal usageRate
    ) {
        if (budget == null || budget.getBudgetAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return BudgetUsageStatus.NO_BUDGET;
        }

        if (payableAmount.compareTo(budget.getBudgetAmount()) > 0) {
            return BudgetUsageStatus.EXCEEDED;
        }

        if (usageRate.compareTo(budget.getWarningThresholdRate()) >= 0) {
            return BudgetUsageStatus.WARNING;
        }

        return BudgetUsageStatus.SAFE;
    }

    // 배송은 조직이 가진 물류 노드 기준으로 조회
// 노드가 없으면 IN 조건 오류를 피하려고 바로 0을 반환
    private long countRelatedShipmentsByStatuses(
            List<Long> organizationNodeIds,
            List<ShipmentStatus> statuses
    ) {
        if (organizationNodeIds.isEmpty()) {
            return 0L;
        }

        return shipmentRepository.countByStatusInAndOriginNodeIdInOrStatusInAndDestinationNodeIdIn(
                statuses,
                organizationNodeIds,
                statuses,
                organizationNodeIds
        );
    }

    // 운영 상태 차트의 한 칸 데이터
    private SettlementStatisticsResponseDto.ChartPointDto buildOperationCountPoint(
            String label,
            String value,
            long count
    ) {
        return SettlementStatisticsResponseDto.ChartPointDto.builder()
                .label(label)
                .value(value)
                .amount(BigDecimal.ZERO)
                .count(count)
                .build();
    }
}
