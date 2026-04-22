package com.ozz.atlas.supply.supplier.service;

import com.ozz.atlas.common.jpa.Status;
import com.ozz.atlas.supply.item.domain.SupplyItem;
import com.ozz.atlas.supply.item.repository.SupplyItemRepository;
import com.ozz.atlas.supply.purchaseorder.repository.PurchaseOrderRepository;
import com.ozz.atlas.supply.subpurchaseorder.repository.SubPurchaseOrderRepository;
import com.ozz.atlas.supply.supplier.domain.ApprovalStatus;
import com.ozz.atlas.supply.supplier.domain.SupplierStatus;
import com.ozz.atlas.supply.supplier.domain.SupplySupplier;
import com.ozz.atlas.supply.supplier.dtos.SupplierListResponse;
import com.ozz.atlas.supply.supplier.dtos.SupplierResponse;
import com.ozz.atlas.supply.supplier.dtos.UpdateSupplierRequest;
import com.ozz.atlas.supply.supplier.exception.SupplierErrorCode;
import com.ozz.atlas.supply.supplier.exception.SupplierException;
import com.ozz.atlas.supply.supplier.relation.service.SupplierRelationService;
import com.ozz.atlas.supply.supplier.repository.SupplierRepository;
import com.ozz.atlas.supply.supplier.search.dtos.SupplierSearchDto;
import com.ozz.atlas.supply.supplier.search.service.SupplierSearchService;
import com.ozz.atlas.supply.supplier.dtos.CreateSupplierRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;
import com.ozz.atlas.supply.supplier.relation.domain.SupplySupplierRelation;
import org.springframework.data.domain.PageImpl;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class SupplierService {

    private final SupplierRepository supplierRepository;

    private final SupplierSearchService supplierSearchService;
    private static final String ADMIN_ROLE = "ADMIN";
    private static final String BUYER_ORGANIZATION_TYPE = "BUYER";
    private static final String SUPPLIER_ORGANIZATION_TYPE = "SUPPLIER";
    private final SupplyItemRepository supplyItemRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SubPurchaseOrderRepository subPurchaseOrderRepository;
    private final SupplierRelationService supplierRelationService;

    public SupplierResponse createSupplier(String userRole, CreateSupplierRequest request) {
        validateAdminCreate(userRole);

        if (supplierRepository.existsByOrganizationPublicId(request.getOrganizationPublicId())) {
            throw new SupplierException(SupplierErrorCode.SUPPLIER_ORGANIZATION_ALREADY_EXISTS);
        }

        if (supplierRepository.existsBySupplierCodeAndSupplierStatusNot(
                request.getSupplierCode(),
                SupplierStatus.TERMINATED
        )) {
            throw new SupplierException(SupplierErrorCode.SUPPLIER_CODE_ALREADY_EXISTS);
        }

        SupplySupplier supplier = SupplySupplier.create(
                request.getOrganizationPublicId(),
                request.getSupplierCode(),
                request.getSupplierName(),
                request.getPrimaryContactName(),
                request.getPrimaryContactEmail(),
                request.getPrimaryContactPhone()
        );

        // 직접 등록이니까 바로 승인 + 활성 처리
        supplier.approve();

        SupplySupplier savedSupplier = supplierRepository.save(supplier);
        supplierSearchService.saveSupplierDocument(savedSupplier);

        return SupplierResponse.fromEntity(savedSupplier);
    }


    @Transactional(readOnly = true)
    public SupplierResponse getSupplier(
            String supplierPublicId,
            String organizationPublicId,
            String organizationType,
            String userRole
    ) {
        SupplySupplier targetSupplier = getApprovedSupplier(supplierPublicId);

        if (canViewAllSuppliers(organizationType, userRole)) {
            return SupplierResponse.fromEntity(targetSupplier);
        }

        SupplySupplier loginSupplier = getLoginSupplier(organizationPublicId, organizationType);

        if (loginSupplier.getId().equals(targetSupplier.getId())) {
            return SupplierResponse.fromEntity(targetSupplier);
        }

        if (!supplierRelationService.hasVisibleRelation(loginSupplier.getId(), targetSupplier.getId())) {
            throw new SupplierException(SupplierErrorCode.ACCESS_DENIED);
        }

        return SupplierResponse.fromEntity(targetSupplier);
    }


    @Transactional(readOnly = true)
    public SupplierResponse getMySupplier(String organizationPublicId, String organizationType) {
        SupplySupplier loginSupplier = getLoginSupplier(organizationPublicId, organizationType);
        return SupplierResponse.fromEntity(loginSupplier);
    }

    @Transactional(readOnly = true)
    public Page<SupplierListResponse> getSupplierList(
            Pageable pageable,
            SupplierSearchDto searchDto,
            String organizationPublicId,
            String organizationType,
            String userRole
    ) {
        if (canViewAllSuppliers(organizationType, userRole)) {
            // 검색 조건이 있으면 ES 통합검색 실행
            if (hasSearchCondition(searchDto)) {
                return supplierSearchService.search(pageable, searchDto)
                        .map(searchResult -> getApprovedSupplier(searchResult.getPublicId()))
                        .map(this::toSupplierListResponse);
            }

//        검색 조건이 없으면 db 목록 조회
            return supplierRepository.findAllByApprovalStatusAndSupplierStatusNot(
                            ApprovalStatus.APPROVED,
                            SupplierStatus.TERMINATED,
                            pageable
                    )
                    .map(this::toSupplierListResponse);
        }

        SupplySupplier loginSupplier = getLoginSupplier(organizationPublicId, organizationType);

        if (hasSearchCondition(searchDto)) {
            throw new SupplierException(SupplierErrorCode.SUPPLIER_SEARCH_FORBIDDEN);
        }

        Map<String, SupplierListResponse> relatedSuppliers = new LinkedHashMap<>();

        for (SupplySupplierRelation relation : supplierRelationService.getVisibleRelations(loginSupplier.getId())) {
            SupplySupplier relatedSupplier = relation.getParentSupplier().getId().equals(loginSupplier.getId())
                    ? relation.getChildSupplier()
                    : relation.getParentSupplier();

            if (!relatedSupplier.getId().equals(loginSupplier.getId())) {
                relatedSuppliers.putIfAbsent(
                        relatedSupplier.getPublicId(),
                        SupplierListResponse.of(
                                relatedSupplier,
                                null,
                                null,
                                null,
                                0L,
                                BigDecimal.ZERO,
                                BigDecimal.ZERO,
                                relation.getRelationStatus().name()
                        )
                );
            }
        }
        return toPage(List.copyOf(relatedSuppliers.values()), pageable);
    }

    public SupplierResponse updateSupplier(String supplierPublicId, String organizationPublicId, UpdateSupplierRequest request) {
        validateOrganizationHeader(organizationPublicId);

        SupplySupplier supplier = getApprovedSupplier(supplierPublicId);
        validateOwner(supplier, organizationPublicId);

        if (supplierRepository.existsBySupplierCodeAndIdNotAndSupplierStatusNot(
                request.getSupplierCode(),
                supplier.getId(),
                SupplierStatus.TERMINATED
        )) {
            throw new SupplierException(SupplierErrorCode.SUPPLIER_CODE_ALREADY_EXISTS);
        }

        supplier.update(
                request.getSupplierCode(),
                request.getSupplierName(),
                request.getPrimaryContactName(),
                request.getPrimaryContactEmail(),
                request.getPrimaryContactPhone()
        );
        // 수정된 협력사 정보를 ES 문서에도 반영
        supplierSearchService.saveSupplierDocument(supplier);

        return SupplierResponse.fromEntity(supplier);
    }

    public void deleteSupplier(String supplierPublicId, String organizationPublicId) {
        validateOrganizationHeader(organizationPublicId);

        SupplySupplier supplier = getApprovedSupplier(supplierPublicId);
        validateOwner(supplier, organizationPublicId);

        List<SupplyItem> items = supplyItemRepository.findAllBySupplier_IdAndStatusIn(
                supplier.getId(),
                List.of(Status.ACTIVE, Status.DEACTIVE)
        );

        for (SupplyItem item : items) {
            item.changeActiveYn(Status.DELETE);
        }
        supplier.softDelete();

        // 종료된 협력사는 ES 검색 결과에서 제거
        supplierSearchService.deleteSupplierDocument(supplier.getId());

    }


    // 검색 조건이 하나라도 들어왔는지 확인
    private boolean hasSearchCondition(SupplierSearchDto searchDto) {
        return searchDto != null
                && (
                // 기본 키워드 검색 여부
                (searchDto.getKeyword() != null && !searchDto.getKeyword().isBlank())

                        // 승인 상태 조건 여부
                        || searchDto.getApprovalStatus() != null

                        // 협력사 상태 조건 여부
                        || searchDto.getSupplierStatus() != null

                        // 조직 기준 조건 여부
                        || (searchDto.getOrganizationPublicId() != null
                        && !searchDto.getOrganizationPublicId().isBlank())

                        // 품목 기준 조건 여부
                        || (searchDto.getItemPublicId() != null
                        && !searchDto.getItemPublicId().isBlank())

                        // 월 생산 가능 수량 조건 여부
                        || searchDto.getMinMonthlyCapacity() != null

                        // 현재 공급 가능 수량 조건 여부
                        || searchDto.getMinAvailableQty() != null

                        // MOQ 조건 여부
                        || searchDto.getMaxMoq() != null

                        // 리드타임 조건 여부
                        || searchDto.getMaxLeadTimeDays() != null

                        // 품질 등급 조건 여부
                        || searchDto.getQualityGrade() != null

                        // 인증서 종류 조건 여부
                        || searchDto.getCertificateTypeId() != null

                        // 인증서 상태 조건 여부
                        || searchDto.getCertificateStatus() != null

                        // 유효한 인증서만 조회할지 여부
                        || searchDto.getOnlyValidCertificate() != null

                        // ESG 등급 조건 여부
                        || searchDto.getEsgGrade() != null

                        // ESG 총점 조건 여부
                        || searchDto.getMinTotalScore() != null
        );
    }

    private SupplySupplier getApprovedSupplier(String supplierPublicId) {
        return supplierRepository.findByPublicIdAndApprovalStatusAndSupplierStatusNot(
                        supplierPublicId,
                        ApprovalStatus.APPROVED,
                        SupplierStatus.TERMINATED
                )
                .orElseThrow(() -> new SupplierException(SupplierErrorCode.SUPPLIER_NOT_FOUND));
    }

    private SupplySupplier getLoginSupplier(String organizationPublicId, String organizationType) {
        validateOrganizationHeader(organizationPublicId);

        if (!SUPPLIER_ORGANIZATION_TYPE.equals(organizationType)) {
            throw new SupplierException(SupplierErrorCode.ACCESS_DENIED);
        }

        SupplySupplier loginSupplier = supplierRepository.findByOrganizationPublicId(organizationPublicId)
                .orElseThrow(() -> new SupplierException(SupplierErrorCode.LOGIN_SUPPLIER_NOT_FOUND));

        if (loginSupplier.getApprovalStatus() != ApprovalStatus.APPROVED
                || loginSupplier.getSupplierStatus() == SupplierStatus.TERMINATED) {
            throw new SupplierException(SupplierErrorCode.LOGIN_SUPPLIER_NOT_FOUND);
        }

        return loginSupplier;
    }


    private void validateOwner(SupplySupplier supplier, String organizationPublicId) {
        if (!supplier.getOrganizationPublicId().equals(organizationPublicId)) {
            throw new SupplierException(SupplierErrorCode.ACCESS_DENIED);
        }
    }

    private boolean canViewAllSuppliers(String organizationType, String userRole) {
        return isAdmin(userRole) || BUYER_ORGANIZATION_TYPE.equals(organizationType);
    }

    private boolean isAdmin(String userRole) {
        return ADMIN_ROLE.equals(userRole);
    }

    private void validateOrganizationHeader(String organizationPublicId) {
        if (organizationPublicId == null || organizationPublicId.isBlank()) {
            throw new SupplierException(SupplierErrorCode.INVALID_ACTOR_HEADER);
        }
    }

    private void validateAdminCreate(String userRole) {
        if (!isAdmin(userRole)) {
            throw new SupplierException(SupplierErrorCode.SUPPLIER_CREATE_FORBIDDEN);
        }
    }

    private SupplierListResponse toSupplierListResponse(SupplySupplier supplier) {
        return SupplierListResponse.of(
                supplier,
                null,
                null,
                null,
                0L,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                resolveListStatus(supplier)
        );
    }

    private String resolveListStatus(SupplySupplier supplier) {
        if (supplier.getApprovalStatus() == ApprovalStatus.REQUESTED) {
            return "REQUESTED";
        }

        if (supplier.getApprovalStatus() == ApprovalStatus.REJECTED) {
            return "REJECTED";
        }

        return supplier.getSupplierStatus().name();
    }

    private <T> Page<T> toPage(List<T> items, Pageable pageable) {
        int start = (int) pageable.getOffset();

        if (start >= items.size()) {
            return new PageImpl<>(List.of(), pageable, items.size());
        }

        int end = Math.min(start + pageable.getPageSize(), items.size());
        return new PageImpl<>(items.subList(start, end), pageable, items.size());
    }








}
