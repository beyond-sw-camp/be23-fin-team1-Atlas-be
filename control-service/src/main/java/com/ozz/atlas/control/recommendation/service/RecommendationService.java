package com.ozz.atlas.control.recommendation.service;

import com.ozz.atlas.control.event.recommendation.RecommendationFailedPayload;
import com.ozz.atlas.control.event.recommendation.RecommendationGeneratedPayload;
import com.ozz.atlas.control.event.recommendation.RecommendationItemPayload;
import com.ozz.atlas.control.recommendation.domain.Recommendation;
import com.ozz.atlas.control.recommendation.domain.RecommendationItem;
import com.ozz.atlas.control.recommendation.dtos.RecommendationItemResponseDto;
import com.ozz.atlas.control.recommendation.dtos.RecommendationResponseDto;
import com.ozz.atlas.control.recommendation.repository.RecommendationRepository;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;

    @Transactional
    public Recommendation saveGenerated(
            RecommendationGeneratedPayload payload,
            String actorUserPublicId,
            String organizationPublicId
    ) {
        // AI 결과 이벤트의 공개 ID를 그대로 권고안 식별자로 사용해 이후 조회/API/event 추적을 단순화한다.
        Recommendation recommendation = recommendationRepository.findByPublicId(payload.recommendationPublicId())
                .orElseGet(() -> Recommendation.generated(
                        payload.recommendationPublicId(),
                        payload.sourceEventId(),
                        payload.sourceEventType(),
                        payload.shipmentPublicId(),
                        payload.riskType(),
                        payload.provider(),
                        payload.model(),
                        payload.modelVersion(),
                        buildGeneratedSummary(payload.recommendations()),
                        actorUserPublicId,
                        organizationPublicId
                ));

        recommendation.applyGenerated(
                payload.sourceEventId(),
                payload.sourceEventType(),
                payload.shipmentPublicId(),
                payload.riskType(),
                payload.provider(),
                payload.model(),
                payload.modelVersion(),
                buildGeneratedSummary(payload.recommendations()),
                actorUserPublicId,
                organizationPublicId
        );
        recommendation.replaceItems(toRecommendationItems(payload.recommendations()));

        return recommendationRepository.save(recommendation);
    }

    @Transactional
    public Recommendation saveFailed(
            RecommendationFailedPayload payload,
            String actorUserPublicId,
            String organizationPublicId
    ) {
        // 실패 이벤트도 동일한 공개 ID로 upsert해 generated/failed 결과를 같은 루트 엔티티에서 추적한다.
        Recommendation recommendation = recommendationRepository.findByPublicId(payload.recommendationPublicId())
                .orElseGet(() -> Recommendation.failed(
                        payload.recommendationPublicId(),
                        payload.sourceEventId(),
                        payload.sourceEventType(),
                        payload.shipmentPublicId(),
                        payload.riskType(),
                        buildFailureSummary(payload),
                        payload.errorMessage(),
                        actorUserPublicId,
                        organizationPublicId
                ));

        recommendation.applyFailed(
                payload.sourceEventId(),
                payload.sourceEventType(),
                payload.shipmentPublicId(),
                payload.riskType(),
                buildFailureSummary(payload),
                payload.errorMessage(),
                actorUserPublicId,
                organizationPublicId
        );

        return recommendationRepository.save(recommendation);
    }

    public Page<RecommendationResponseDto> getRecommendations(String organizationPublicId, Pageable pageable) {
        return recommendationRepository.findByOrganizationPublicIdOrderByCreatedAtDesc(organizationPublicId, pageable)
                .map(this::toResponseDto);
    }

    public RecommendationResponseDto getRecommendation(String publicId, String organizationPublicId) {
        Recommendation recommendation = recommendationRepository.findByPublicIdAndOrganizationPublicId(publicId, organizationPublicId)
                .orElseThrow(() -> new IllegalArgumentException("권고안을 찾을 수 없습니다. publicId=" + publicId));

        return toResponseDto(recommendation);
    }

    private List<RecommendationItem> toRecommendationItems(List<RecommendationItemPayload> payloads) {
        if (payloads == null || payloads.isEmpty()) {
            return List.of();
        }

        // payload 순서를 보존해야 화면과 로그에서 AI가 제안한 원래 순서를 그대로 재현할 수 있다.
        return IntStream.range(0, payloads.size())
                .mapToObj(index -> {
                    RecommendationItemPayload payload = payloads.get(index);
                    return RecommendationItem.create(
                            index + 1,
                            payload.title(),
                            payload.reason(),
                            payload.action(),
                            payload.priority(),
                            payload.confidence()
                    );
                })
                .toList();
    }

    private String buildGeneratedSummary(List<RecommendationItemPayload> payloads) {
        if (payloads == null || payloads.isEmpty()) {
            return "생성된 권고안이 없습니다.";
        }

        String joinedTitles = payloads.stream()
                .limit(3)
                .map(RecommendationItemPayload::title)
                .collect(Collectors.joining(", "));

        return "권고안 %d건이 생성되었습니다: %s".formatted(payloads.size(), joinedTitles);
    }

    private String buildFailureSummary(RecommendationFailedPayload payload) {
        return "%s 리스크에 대한 권고안 생성에 실패했습니다.".formatted(payload.riskType());
    }

    private RecommendationResponseDto toResponseDto(Recommendation recommendation) {
        return RecommendationResponseDto.builder()
                .publicId(recommendation.getPublicId())
                .sourceEventId(recommendation.getSourceEventId())
                .sourceEventType(recommendation.getSourceEventType())
                .shipmentPublicId(recommendation.getShipmentPublicId())
                .riskType(recommendation.getRiskType())
                .recommendationStatus(recommendation.getRecommendationStatus())
                .provider(recommendation.getProvider())
                .model(recommendation.getModel())
                .modelVersion(recommendation.getModelVersion())
                .summary(recommendation.getSummary())
                .failureReason(recommendation.getFailureReason())
                .items(recommendation.getItems().stream()
                        .map(this::toItemResponseDto)
                        .toList())
                .createdAt(recommendation.getCreatedAt())
                .updatedAt(recommendation.getUpdatedAt())
                .build();
    }

    private RecommendationItemResponseDto toItemResponseDto(RecommendationItem item) {
        return RecommendationItemResponseDto.builder()
                .sequenceNo(item.getSequenceNo())
                .title(item.getTitle())
                .reason(item.getReason())
                .action(item.getAction())
                .priority(item.getPriority())
                .confidence(item.getConfidence())
                .build();
    }
}
