package com.ozz.atlas.supply.returns.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import com.ozz.atlas.supply.returns.domain.ReturnStatus;
import com.ozz.atlas.supply.returns.domain.ReturnStatusHistory;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "상태 응답")
public class ReturnStatusHistoryResponseDto {
    @Schema(description = "식별자", example = "1", nullable = true)
    private Long id;
    @Schema(description = "식별자", example = "1", nullable = true)
    private Long returnRequestId;
    @Schema(description = "상태", example = "ACTIVE", nullable = true)
    private ReturnStatus beforeStatus;
    @Schema(description = "상태", example = "ACTIVE", nullable = true)
    private ReturnStatus afterStatus;
    @Schema(description = "사유", example = "샘플 내용", nullable = true)
    private String reason;
    @Schema(description = "recorded At 값", example = "2026-05-08T10:00:00", nullable = true)
    private LocalDateTime recordedAt;
    @Schema(description = "recorded By 값", example = "sample", nullable = true)
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