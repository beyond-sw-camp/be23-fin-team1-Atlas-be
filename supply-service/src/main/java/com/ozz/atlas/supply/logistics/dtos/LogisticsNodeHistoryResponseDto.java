package com.ozz.atlas.supply.logistics.dtos;

import com.ozz.atlas.supply.logistics.domain.LogisticsNodeCapacityStatus;
import com.ozz.atlas.supply.logistics.domain.LogisticsNodeHistory;
import com.ozz.atlas.supply.logistics.domain.LogisticsNodeHistoryChangeType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class LogisticsNodeHistoryResponseDto {

    private Long id;
    private String logisticsNodePublicId;
    private LogisticsNodeHistoryChangeType actionType;
    private String changeType;
    private LogisticsNodeCapacityStatus beforeCapacityStatus;
    private LogisticsNodeCapacityStatus afterCapacityStatus;
    private Boolean beforeActive;
    private Boolean afterActive;
    private String nodeName;
    private String address;
    private String memo;
    private LocalDateTime recordedAt;
    private String processedByUserPublicId;
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
