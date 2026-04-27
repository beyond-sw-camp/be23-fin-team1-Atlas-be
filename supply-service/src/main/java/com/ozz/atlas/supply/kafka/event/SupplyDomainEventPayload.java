package com.ozz.atlas.supply.kafka.event;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공급망 도메인 Kafka 이벤트 payload")
public record SupplyDomainEventPayload(
        @Schema(description = "참조 도메인 공개 식별자", example = "01HS6Q1M4F4BCJ0YV7Z5X8W9KB")
        String referencePublicId,
        @Schema(description = "업무 번호", example = "PO-2026-0001", nullable = true)
        String referenceNumber,
        @Schema(description = "업무 상태", example = "CREATED", nullable = true)
        String status,
        @Schema(description = "이벤트 표시명", example = "발주 생성")
        String eventName,
        @Schema(description = "이벤트 설명", example = "발주 생성 시")
        String description,
        @Schema(description = "반품 유형", example = "DEFECTIVE", nullable = true)
        String returnType,
        String rootPurchaseOrderPublicId,
        String rootBuyerOrganizationPublicId,
        String directBuyerOrganizationPublicId,
        String directSupplierOrganizationPublicId,
        String parentPurchaseOrderPublicId,
        String subPurchaseOrderPublicId
) {
}
