package com.ozz.atlas.supply.supplier.service;

import com.ozz.atlas.supply.supplier.domain.ApprovalStatus;
import com.ozz.atlas.supply.supplier.domain.SupplierStatus;
import com.ozz.atlas.supply.supplier.domain.SupplySupplier;
import com.ozz.atlas.supply.supplier.dtos.SupplierResponse;
import com.ozz.atlas.supply.supplier.dtos.UpdateSupplierRequest;
import com.ozz.atlas.supply.supplier.repository.SupplierRepository;
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
    public Page<SupplierResponse> getSupplierList(Pageable pageable) {
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

        if (supplierRepository.existsBySupplierCodeAndPublicIdNotAndSupplierStatusNot(
                request.getSupplierCode(),
                supplierPublicId,
                SupplierStatus.TERMINATED
        )) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 존재하는 협력사 코드입니다.");
        }

        supplier.update(
                request.getSupplierCode(),
                request.getSupplierName(),
                request.getTierLevel(),
                request.getPrimaryContactName(),
                request.getPrimaryContactEmail(),
                request.getPrimaryContactPhone()
        );

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
    }
}
