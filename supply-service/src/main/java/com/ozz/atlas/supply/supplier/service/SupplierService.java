package com.ozz.atlas.supply.supplier.service;

import com.ozz.atlas.supply.supplier.domain.ApprovalStatus;
import com.ozz.atlas.supply.supplier.domain.SupplierStatus;
import com.ozz.atlas.supply.supplier.domain.SupplySupplier;
import com.ozz.atlas.supply.supplier.dtos.SupplierResponse;
import com.ozz.atlas.supply.supplier.dtos.UpdateSupplierRequest;
import com.ozz.atlas.supply.supplier.repository.SupplierRepository;
import com.ozz.atlas.supply.supplier.search.dtos.SupplierSearchDto;
import com.ozz.atlas.supply.supplier.search.service.SupplierSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional
public class SupplierService {

    private final SupplierRepository supplierRepository;

    private final SupplierSearchService supplierSearchService;

    @Transactional(readOnly = true)
    public SupplierResponse getSupplier(String supplierPublicId) {
        SupplySupplier supplier = supplierRepository.findByPublicIdAndApprovalStatusAndSupplierStatusNot(
                        supplierPublicId,
                        ApprovalStatus.APPROVED,
                        SupplierStatus.TERMINATED
                )
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 협력사가 존재하지 않습니다."));

        return SupplierResponse.fromEntity(supplier);
    }

    @Transactional(readOnly = true)
    public Page<SupplierResponse> getSupplierList(Pageable pageable, SupplierSearchDto searchDto) {

        // 검색 조건이 있으면 ES 통합검색 실행
        if (hasSearchCondition(searchDto)) {
            return supplierSearchService.search(pageable, searchDto);
        }

//        검색 조건이 없으면 db 목록 조회
        return supplierRepository.findAllByApprovalStatusAndSupplierStatusNot(
                        ApprovalStatus.APPROVED,
                        SupplierStatus.TERMINATED,
                        pageable
                )
                .map(SupplierResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<SupplierResponse> getSuppliersByTierLevel(Integer tierLevel, Pageable pageable) {
        return supplierRepository.findAllByTierLevelAndApprovalStatusAndSupplierStatusNot(
                        tierLevel,
                        ApprovalStatus.APPROVED,
                        SupplierStatus.TERMINATED,
                        pageable
                )
                .map(SupplierResponse::fromEntity);
    }

    public SupplierResponse updateSupplier(String supplierPublicId, UpdateSupplierRequest request) {
        SupplySupplier supplier = supplierRepository.findByPublicIdAndApprovalStatusAndSupplierStatusNot(
                        supplierPublicId,
                        ApprovalStatus.APPROVED,
                        SupplierStatus.TERMINATED
                )
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 협력사가 존재하지 않습니다."));

        if (supplierRepository.existsBySupplierCodeAndIdNotAndSupplierStatusNot(
                request.getSupplierCode(),
                supplier.getId(),
                SupplierStatus.TERMINATED
        )) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "...");
        }

        supplier.update(
                request.getSupplierCode(),
                request.getSupplierName(),
                request.getTierLevel(),
                request.getPrimaryContactName(),
                request.getPrimaryContactEmail(),
                request.getPrimaryContactPhone()
        );

        // 수정된 협력사 정보를 ES 문서에도 반영
        supplierSearchService.saveSupplierDocument(supplier);

        return SupplierResponse.fromEntity(supplier);
    }

    public void deleteSupplier(String supplierPublicId) {
        SupplySupplier supplier = supplierRepository.findByPublicIdAndApprovalStatusAndSupplierStatusNot(
                        supplierPublicId,
                        ApprovalStatus.APPROVED,
                        SupplierStatus.TERMINATED
                )
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 협력사가 존재하지 않습니다."));

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

                        // 협력사 단계 조건 여부
                        || searchDto.getTierLevel() != null

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

}
