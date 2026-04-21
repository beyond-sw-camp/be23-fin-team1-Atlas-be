package com.ozz.atlas.control.recommendation.dtos;

import com.ozz.atlas.control.recommendation.domain.RecommendationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
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
@Schema(description = "권고안 응답")
public class RecommendationResponseDto {

    @Schema(description = "권고안 공개 식별자", example = "01KPPQ2N7Q9P4H2R8V4QHTR7S9")
    private String publicId;

    @Schema(description = "원본 이벤트 ID", example = "01KPPPEBTZRBJ02YPGB0XHWETC")
    private String sourceEventId;

    @Schema(description = "원본 이벤트 타입", example = "shipment.delay-detected")
    private String sourceEventType;

    @Schema(description = "출하 공개 식별자", example = "01KPPPDYPH4Z7J4MYZ1439KNBA")
    private String shipmentPublicId;

    @Schema(description = "리스크 유형", example = "shipment_delayed")
    private String riskType;

    @Schema(description = "권고안 상태", example = "GENERATED")
    private RecommendationStatus recommendationStatus;

    @Schema(description = "AI 제공자", example = "local-llm", nullable = true)
    private String provider;

    @Schema(description = "모델명", example = "supergemma4-e4b-abliterated-mlx", nullable = true)
    private String model;

    @Schema(description = "모델 버전", example = "2026-04-21", nullable = true)
    private String modelVersion;

    @Schema(description = "권고안 요약", example = "권고안 3건이 생성되었습니다: 대체 허브 경유, 긴급 선적 전환, 납기 재조정")
    private String summary;

    @Schema(description = "실패 사유", example = "모델 응답 생성에 실패했습니다.", nullable = true)
    private String failureReason;

    @Schema(description = "권고안 항목 목록")
    private List<RecommendationItemResponseDto> items;

    @Schema(description = "생성 시각", example = "2026-04-21T14:55:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정 시각", example = "2026-04-21T14:56:00")
    private LocalDateTime updatedAt;
}
