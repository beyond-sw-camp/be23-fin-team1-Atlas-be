package com.ozz.atlas.control.recommendation.controller;

import com.ozz.atlas.control.recommendation.dtos.RecommendationResponseDto;
import com.ozz.atlas.control.recommendation.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/control/recommendations")
@RequiredArgsConstructor
@Tag(name = "Recommendation", description = "AI 권고안 조회 API")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping
    @Operation(
            summary = "권고안 목록 조회",
            description = "조직 기준으로 생성된 권고안 목록을 최신순으로 조회한다.",
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            schema = @Schema(implementation = RecommendationResponseDto.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "content": [
                                                {
                                                  "publicId": "01KPPQ2N7Q9P4H2R8V4QHTR7S9",
                                                  "sourceEventId": "01KPPPEBTZRBJ02YPGB0XHWETC",
                                                  "sourceEventType": "shipment.delay-detected",
                                                  "shipmentPublicId": "01KPPPDYPH4Z7J4MYZ1439KNBA",
                                                  "riskType": "shipment_delayed",
                                                  "recommendationStatus": "GENERATED",
                                                  "provider": "local-llm",
                                                  "model": "supergemma4-e4b-abliterated-mlx",
                                                  "modelVersion": "2026-04-21",
                                                  "summary": "권고안 3건이 생성되었습니다: 대체 허브 경유, 긴급 선적 전환, 납기 재조정",
                                                  "failureReason": null,
                                                  "items": [],
                                                  "createdAt": "2026-04-21T14:55:00",
                                                  "updatedAt": "2026-04-21T14:55:10"
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            )
    )
    public ResponseEntity<Page<RecommendationResponseDto>> getRecommendations(
            @Parameter(description = "조직 공개 식별자", example = "org00000000000000000000001", required = true)
            @RequestHeader("X-Organization-Public-Id") String organizationPublicId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(recommendationService.getRecommendations(organizationPublicId, pageable));
    }

    @GetMapping("/{recommendationPublicId}")
    @Operation(
            summary = "권고안 상세 조회",
            description = "조직 기준으로 특정 권고안의 상세 내용을 조회한다.",
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            schema = @Schema(implementation = RecommendationResponseDto.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "publicId": "01KPPQ2N7Q9P4H2R8V4QHTR7S9",
                                              "sourceEventId": "01KPPPEBTZRBJ02YPGB0XHWETC",
                                              "sourceEventType": "shipment.delay-detected",
                                              "shipmentPublicId": "01KPPPDYPH4Z7J4MYZ1439KNBA",
                                              "riskType": "shipment_delayed",
                                              "recommendationStatus": "GENERATED",
                                              "provider": "local-llm",
                                              "model": "supergemma4-e4b-abliterated-mlx",
                                              "modelVersion": "2026-04-21",
                                              "summary": "권고안 3건이 생성되었습니다: 대체 허브 경유, 긴급 선적 전환, 납기 재조정",
                                              "failureReason": null,
                                              "items": [
                                                {
                                                  "sequenceNo": 1,
                                                  "title": "대체 허브 경유로 배송 경로 변경",
                                                  "reason": "현재 허브 적체로 예상 도착 지연이 커지고 있습니다.",
                                                  "action": "부산 허브 대신 대전 허브로 우회합니다.",
                                                  "priority": 1,
                                                  "confidence": 0.87
                                                }
                                              ],
                                              "createdAt": "2026-04-21T14:55:00",
                                              "updatedAt": "2026-04-21T14:55:10"
                                            }
                                            """
                            )
                    )
            )
    )
    public ResponseEntity<RecommendationResponseDto> getRecommendation(
            @Parameter(description = "권고안 공개 식별자", example = "01KPPQ2N7Q9P4H2R8V4QHTR7S9", required = true)
            @PathVariable String recommendationPublicId,
            @Parameter(description = "조직 공개 식별자", example = "org00000000000000000000001", required = true)
            @RequestHeader("X-Organization-Public-Id") String organizationPublicId
    ) {
        return ResponseEntity.ok(recommendationService.getRecommendation(recommendationPublicId, organizationPublicId));
    }

    @PostMapping("/{recommendationPublicId}/accept")
    @Operation(
            summary = "권고안 수락",
            description = "GENERATED 상태의 권고안을 수락하고 recommendation.accepted 이벤트를 outbox에 적재한다.",
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "수락 성공",
                    content = @Content(schema = @Schema(implementation = RecommendationResponseDto.class))
            )
    )
    public ResponseEntity<RecommendationResponseDto> acceptRecommendation(
            @Parameter(description = "권고안 공개 식별자", example = "01KPPQ2N7Q9P4H2R8V4QHTR7S9", required = true)
            @PathVariable String recommendationPublicId,
            @Parameter(description = "조직 공개 식별자", example = "org00000000000000000000001", required = true)
            @RequestHeader("X-Organization-Public-Id") String organizationPublicId,
            @Parameter(description = "의사결정 사용자 공개 식별자", example = "usr00000000000000000000001", required = true)
            @RequestHeader("X-User-Public-Id") String actorUserPublicId
    ) {
        return ResponseEntity.ok(
                recommendationService.accept(recommendationPublicId, organizationPublicId, actorUserPublicId)
        );
    }

    @PostMapping("/{recommendationPublicId}/reject")
    @Operation(
            summary = "권고안 거절",
            description = "GENERATED 상태의 권고안을 거절하고 recommendation.rejected 이벤트를 outbox에 적재한다.",
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "거절 성공",
                    content = @Content(schema = @Schema(implementation = RecommendationResponseDto.class))
            )
    )
    public ResponseEntity<RecommendationResponseDto> rejectRecommendation(
            @Parameter(description = "권고안 공개 식별자", example = "01KPPQ2N7Q9P4H2R8V4QHTR7S9", required = true)
            @PathVariable String recommendationPublicId,
            @Parameter(description = "조직 공개 식별자", example = "org00000000000000000000001", required = true)
            @RequestHeader("X-Organization-Public-Id") String organizationPublicId,
            @Parameter(description = "의사결정 사용자 공개 식별자", example = "usr00000000000000000000001", required = true)
            @RequestHeader("X-User-Public-Id") String actorUserPublicId
    ) {
        return ResponseEntity.ok(
                recommendationService.reject(recommendationPublicId, organizationPublicId, actorUserPublicId)
        );
    }
}
