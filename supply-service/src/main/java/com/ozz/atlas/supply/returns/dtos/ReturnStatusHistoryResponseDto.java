package com.ozz.atlas.supply.returns.dtos;

import com.ozz.atlas.supply.returns.domain.ReturnStatus;
import com.ozz.atlas.supply.returns.domain.ReturnStatusHistory;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReturnStatusHistoryResponseDto {
    private Long id;
    private Long returnRequestId;
    private ReturnStatus beforeStatus;
    private ReturnStatus afterStatus;
    private String reason;
    private LocalDateTime recordedAt;
    private String recordedBy;

    public static ReturnStatusHistoryResponseDto from(ReturnStatusHistory entity) {
        return ReturnStatusHistoryResponseDto.builder()
                .id(entity.getId())
                .returnRequestId(entity.getReturnRequestId())
                .beforeStatus(entity.getBeforeStatus())
                .afterStatus(entity.getAfterStatus())
                .reason(entity.getReason())
                .recordedAt(entity.getRecordedAt())
                .recordedBy(entity.getRecordedBy())
                .build();
    }
}