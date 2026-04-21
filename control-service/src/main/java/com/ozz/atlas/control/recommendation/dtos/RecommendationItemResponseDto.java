package com.ozz.atlas.control.recommendation.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "권고안 개별 항목 응답")
public class RecommendationItemResponseDto {

    @Schema(description = "권고안 순서", example = "1")
    private int sequenceNo;

    @Schema(description = "권고안 제목", example = "대체 허브 경유로 배송 경로 변경")
    private String title;

    @Schema(description = "권고안 근거", example = "현재 허브 적체로 예상 도착 지연이 커지고 있습니다.")
    private String reason;

    @Schema(description = "실행 액션 제안", example = "부산 허브 대신 대전 허브로 우회합니다.")
    private String action;

    @Schema(description = "우선순위", example = "1")
    private int priority;

    @Schema(description = "신뢰도", example = "0.87")
    private double confidence;
}
