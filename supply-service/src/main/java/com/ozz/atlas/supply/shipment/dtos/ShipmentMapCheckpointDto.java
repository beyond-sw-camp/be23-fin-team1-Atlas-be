package com.ozz.atlas.supply.shipment.dtos;

import com.ozz.atlas.supply.shipment.domain.CheckpointStatus;
import com.ozz.atlas.supply.shipment.domain.CheckpointType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Schema(description = "출하 지도 체크포인트 응답")
public class ShipmentMapCheckpointDto {

    @Schema(description = "체크포인트 노드 공개 식별자", example = "node_01HZY1AAA")
    private String nodePublicId;

    @Schema(description = "체크포인트 노드명", example = "서울 물류창고")
    private String nodeName;

    @Schema(description = "체크포인트 노드 코드", example = "WH-CHO1-003")
    private String nodeCode;

    @Schema(description = "체크포인트 유형", example = "DEPARTURE")
    private CheckpointType checkpointType;

    @Schema(description = "체크포인트 상태", example = "PASSED")
    private CheckpointStatus checkpointStatus;

    @Schema(description = "예정 시각", example = "2026-04-25T09:00:00", nullable = true)
    private LocalDateTime plannedAt;

    @Schema(description = "실제 시각", example = "2026-04-25T09:05:00", nullable = true)
    private LocalDateTime actualAt;

    @Schema(description = "위도", example = "37.5000242", nullable = true)
    private BigDecimal latitude;

    @Schema(description = "경도", example = "127.0365086", nullable = true)
    private BigDecimal longitude;

    @Schema(description = "메모", example = "출발 완료", nullable = true)
    private String note;
}
