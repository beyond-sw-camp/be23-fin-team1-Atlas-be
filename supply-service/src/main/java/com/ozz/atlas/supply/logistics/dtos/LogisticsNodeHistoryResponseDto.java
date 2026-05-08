package com.ozz.atlas.supply.logistics.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import com.ozz.atlas.supply.logistics.domain.LogisticsNodeCapacityStatus;
import com.ozz.atlas.supply.logistics.domain.LogisticsNodeHistory;
import com.ozz.atlas.supply.logistics.domain.LogisticsNodeHistoryChangeType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "Logistics Node History 값 응답")
public class LogisticsNodeHistoryResponseDto {

    @Schema(description = "식별자", example = "1", nullable = true)
    private Long id;
    @Schema(description = "물류 노드 공개 식별자", example = "sample_public_id", nullable = true)
    private String logisticsNodePublicId;
    @Schema(description = "유형", example = "DEFAULT", nullable = true)
    private LogisticsNodeHistoryChangeType actionType;
    @Schema(description = "유형", example = "DEFAULT", nullable = true)
    private String changeType;
    @Schema(description = "상태", example = "ACTIVE", nullable = true)
    private LogisticsNodeCapacityStatus beforeCapacityStatus;
    @Schema(description = "상태", example = "ACTIVE", nullable = true)
    private LogisticsNodeCapacityStatus afterCapacityStatus;
    @Schema(description = "before Active 값", example = "true", nullable = true)
    private Boolean beforeActive;
    @Schema(description = "after Active 값", example = "true", nullable = true)
    private Boolean afterActive;
    @Schema(description = "이름", example = "샘플 이름", nullable = true)
    private String nodeName;
    @Schema(description = "address 값", example = "sample", nullable = true)
    private String address;
    @Schema(description = "메모", example = "샘플 내용", nullable = true)
    private String memo;
    @Schema(description = "recorded At 값", example = "2026-05-08T10:00:00", nullable = true)
    private LocalDateTime recordedAt;
    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String processedByUserPublicId;
    @Schema(description = "이름", example = "샘플 이름", nullable = true)
    private String processedByUserName;
    public static LogisticsNodeHistoryResponseDto from(LogisticsNodeHistory history) {
        return from(history, null);
    }
    public static LogisticsNodeHistoryResponseDto from(LogisticsNodeHistory history, String processedByUserName) {
        return LogisticsNodeHistoryResponseDto.builder()
                .id(history.getId())
                .logisticsNodePublicId(history.getLogisticsNodePublicId())
                .actionType(history.getChangeType())
                .changeType(history.getChangeType().getLabel())
                .beforeCapacityStatus(history.getBeforeCapacityStatus())
                .afterCapacityStatus(history.getAfterCapacityStatus())
                .beforeActive(history.getBeforeActive())
                .afterActive(history.getAfterActive())
                .nodeName(history.getNodeName())
                .address(history.getAddress())
                .memo(history.getMemo())
                .recordedAt(history.getRecordedAt())
                .processedByUserPublicId(history.getRecordedBy())
                .processedByUserName(processedByUserName)
                .build();
    }
}
