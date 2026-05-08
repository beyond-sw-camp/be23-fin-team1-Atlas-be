package com.ozz.atlas.supply.shipment.dtos;

import com.ozz.atlas.supply.shipment.domain.ShipmentSourceType;
import com.ozz.atlas.supply.shipment.domain.ShipmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Schema(description = "출하 목록 응답")
public class ShipmentListResponseDto {

    @Schema(description = "출하 공개 식별자", example = "ship_01HZY1SHIPMENT123456789")
    private String publicId;
    @Schema(description = "출하 번호", example = "SHIP-2026-0001")
    private String shipmentNumber;
    @Schema(description = "출하 유형", example = "ORDER")
    private ShipmentSourceType sourceType;
    @Schema(description = "출하 기준 공개 식별자", example = "01HZY1SOURCE12345678901234", nullable = true)
    private String sourcePublicId;
    @Schema(description = "상위 발주 공개 식별자", example = "po_01HZY1PO123456789", nullable = true)
    private String purchaseOrderPublicId;
    @Schema(description = "하위 발주 공개 식별자", example = "subpo_01HZY1SUBPO123456789", nullable = true)
    private String subPurchaseOrderPublicId;
    @Schema(description = "운송사명", example = "CJ Logistics")
    private String carrierName;
    @Schema(description = "출발 물류거점 공개 식별자", example = "01HQNODEORIGIN123456789")
    private String originNodePublicId;
    @Schema(description = "출발 물류거점명", example = "서울 물류창고")
    private String originNodeName;
    @Schema(description = "출발 물류거점 코드", example = "WH-CHO1-003")
    private String originNodeCode;
    @Schema(description = "도착 물류 노드 공개 식별자", example = "node_dest_01HZY1BBB")
    private String destinationNodePublicId;
    @Schema(description = "도착 물류거점명", example = "부산 물류창고")
    private String destinationNodeName;
    @Schema(description = "도착 물류거점 코드", example = "WH-BUY1-001")
    private String destinationNodeCode;
    @Schema(description = "현재 물류 노드 공개 식별자", example = "node_hub_01HZY1CCC", nullable = true)
    private String currentNodePublicId;
    @Schema(description = "현재 물류거점명", example = "대전 허브", nullable = true)
    private String currentNodeName;
    @Schema(description = "현재 물류거점 코드", example = "WH-HUB1-002", nullable = true)
    private String currentNodeCode;
    @Schema(description = "예상 출발 시각", example = "2026-04-18T08:00:00")
    private LocalDateTime departureEta;
    @Schema(description = "예상 도착 시각", example = "2026-04-18T14:00:00")
    private LocalDateTime arrivalEta;
    @Schema(description = "출하 상태", example = "IN_TRANSIT")
    private ShipmentStatus status;
    @Schema(description = "온도 관리 필요 여부", example = "true")
    private boolean temperatureRequired;
    @Schema(description = "밀봉 포장 필요 여부", example = "true")
    private boolean sealedPackagingRequired;
    @Schema(description = "파손 위험 여부", example = "true")
    private boolean fragile;
    @Schema(description = "현재 조직의 출하 정보 수정 가능 여부", example = "true")
    private boolean canUpdate;
    @Schema(description = "현재 조직의 배송중 처리 가능 여부", example = "true")
    private boolean canStart;
    @Schema(description = "현재 조직의 도착완료 처리 가능 여부", example = "false")
    private boolean canArrive;
    @Schema(description = "현재 조직의 출하 취소 가능 여부", example = "true")
    private boolean canCancel;
    @Schema(description = "현재 조직의 위치 등록 가능 여부", example = "false")
    private boolean canTrack;
    @Schema(description = "현재 조직의 배송 예외 등록 가능 여부", example = "true")
    private boolean canRegisterException;
    @Schema(description = "반품 이력 존재 여부", example = "false")
    private boolean hasReturn;

}
