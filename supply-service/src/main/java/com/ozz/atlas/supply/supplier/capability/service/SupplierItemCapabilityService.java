package com.ozz.atlas.supply.supplier.capability.service;

import com.ozz.atlas.common.jpa.Status;
import com.ozz.atlas.supply.item.domain.SupplyItem;
import com.ozz.atlas.supply.item.repository.SupplyItemRepository;
import com.ozz.atlas.supply.supplier.capability.domain.SupplySupplierItemCapability;
import com.ozz.atlas.supply.supplier.capability.dtos.CreateSupplierItemCapabilityRequest;
import com.ozz.atlas.supply.supplier.capability.dtos.SupplierItemCapabilityResponse;
import com.ozz.atlas.supply.supplier.capability.dtos.UpdateSupplierItemCapabilityRequest;
import com.ozz.atlas.supply.supplier.capability.exception.SupplierItemCapabilityErrorCode;
import com.ozz.atlas.supply.supplier.capability.exception.SupplierItemCapabilityException;
import com.ozz.atlas.supply.supplier.capability.repository.SupplierItemCapabilityRepository;
import com.ozz.atlas.supply.supplier.domain.ApprovalStatus;
import com.ozz.atlas.supply.supplier.domain.SupplierStatus;
import com.ozz.atlas.supply.supplier.domain.SupplySupplier;
import com.ozz.atlas.supply.supplier.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SupplierItemCapabilityService {

    private final SupplierItemCapabilityRepository capabilityRepository;
    private final SupplierRepository supplierRepository;
    private final SupplyItemRepository supplyItemRepository;

    public SupplierItemCapabilityResponse createCapability(
            String supplierPublicId,
            CreateSupplierItemCapabilityRequest request
    ) {
        SupplySupplier supplier = getApprovedSupplier(supplierPublicId);
        SupplyItem item = getActiveItem(request.getItemPublicId());

        // supplier, item 모두 내부 FK 엔티티이므로 중복 체크는 id 기준으로 처리한다.
        if (capabilityRepository.existsBySupplier_IdAndItem_Id(supplier.getId(), item.getId())) {
            throw new SupplierItemCapabilityException(SupplierItemCapabilityErrorCode.CAPABILITY_ALREADY_EXISTS);
        }

        SupplySupplierItemCapability capability = SupplySupplierItemCapability.create(
                supplier,
                item,
                request.getLeadTimeDays(),
                request.getMonthlyCapacity(),
                request.getAvailableQty(),
                request.getMoq(),
                request.getQualityGrade(),
                request.getUnitPriceHint(),
                request.getValidFrom(),
                request.getPartialConfirmationAllowed()
        );


        return SupplierItemCapabilityResponse.fromEntity(capabilityRepository.save(capability));
    }

    @Transactional(readOnly = true)
    public List<SupplierItemCapabilityResponse> getCapabilities(String supplierPublicId) {
        SupplySupplier supplier = getApprovedSupplier(supplierPublicId);

        return capabilityRepository.findAllBySupplier_IdOrderByItem_ItemNameAsc(supplier.getId())
                .stream()
                .map(SupplierItemCapabilityResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public SupplierItemCapabilityResponse getCapability(String supplierPublicId, String itemPublicId) {
        SupplySupplier supplier = getApprovedSupplier(supplierPublicId);
        SupplyItem item = getActiveItem(itemPublicId);

        SupplySupplierItemCapability capability = capabilityRepository.findBySupplier_IdAndItem_Id(
                        supplier.getId(),
                        item.getId()
                )
                .orElseThrow(() -> new SupplierItemCapabilityException(SupplierItemCapabilityErrorCode.CAPABILITY_NOT_FOUND));

        return SupplierItemCapabilityResponse.fromEntity(capability);
    }

    public SupplierItemCapabilityResponse updateCapability(
            String supplierPublicId,
            String itemPublicId,
            UpdateSupplierItemCapabilityRequest request
    ) {
        if (isEmptyPatch(request)) {
            throw new SupplierItemCapabilityException(SupplierItemCapabilityErrorCode.EMPTY_PATCH_NOT_ALLOWED);
        }

        SupplySupplier supplier = getApprovedSupplier(supplierPublicId);
        SupplyItem item = getActiveItem(itemPublicId);

        SupplySupplierItemCapability capability = capabilityRepository.findBySupplier_IdAndItem_Id(
                        supplier.getId(),
                        item.getId()
                )
                .orElseThrow(() -> new SupplierItemCapabilityException(SupplierItemCapabilityErrorCode.CAPABILITY_NOT_FOUND));

        capability.update(
                request.getLeadTimeDays(),
                request.getMonthlyCapacity(),
                request.getAvailableQty(),
                request.getMoq(),
                request.getQualityGrade(),
                request.getUnitPriceHint(),
                request.getValidFrom(),
                request.getPartialConfirmationAllowed()
        );


        return SupplierItemCapabilityResponse.fromEntity(capability);
    }

    private SupplySupplier getApprovedSupplier(String supplierPublicId) {
        return supplierRepository.findByPublicIdAndApprovalStatusAndSupplierStatusNot(
                        supplierPublicId,
                        ApprovalStatus.APPROVED,
                        SupplierStatus.TERMINATED
                )
                .orElseThrow(() -> new SupplierItemCapabilityException(SupplierItemCapabilityErrorCode.SUPPLIER_NOT_FOUND));
    }

    private SupplyItem getActiveItem(String itemPublicId) {
        return supplyItemRepository.findByPublicIdAndStatusIn(itemPublicId, List.of(Status.ACTIVE))
                .orElseThrow(() -> new SupplierItemCapabilityException(SupplierItemCapabilityErrorCode.ITEM_NOT_FOUND));
    }

    private boolean isEmptyPatch(UpdateSupplierItemCapabilityRequest request) {
        return request.getLeadTimeDays() == null
                && request.getMonthlyCapacity() == null
                && request.getAvailableQty() == null
                && request.getMoq() == null
                && request.getQualityGrade() == null
                && request.getUnitPriceHint() == null
                && request.getValidFrom() == null
                && request.getPartialConfirmationAllowed() == null;
    }
}
