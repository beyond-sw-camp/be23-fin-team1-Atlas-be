package com.ozz.atlas.control.kafka.monitoring.controller;

import com.ozz.atlas.control.kafka.monitoring.dtos.KafkaEventSummaryResponse;
import com.ozz.atlas.control.kafka.monitoring.dtos.KafkaEventRuleToggleRequest;
import com.ozz.atlas.control.kafka.monitoring.dtos.KafkaEventSummaryUpsertRequest;
import com.ozz.atlas.control.kafka.monitoring.dtos.KafkaSubscriptionStatusResponse;
import com.ozz.atlas.control.kafka.monitoring.dtos.PageResponse;
import com.ozz.atlas.control.kafka.monitoring.service.KafkaMonitoringService;
import com.ozz.atlas.control.kafka.rule.service.KafkaEventRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ozz.atlas.control.kafka.log.search.dtos.EventLogSearchDto;
import com.ozz.atlas.control.kafka.log.search.dtos.EventLogSearchResponse;
import com.ozz.atlas.control.kafka.log.search.service.EventLogSearchService;


import java.util.List;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/control/monitoring/kafka")
@RequiredArgsConstructor
@Tag(name = "KafkaMonitoring")
public class KafkaMonitoringController {

    private final KafkaMonitoringService kafkaMonitoringService;
    private final KafkaEventRuleService kafkaEventRuleService;
    private final EventLogSearchService eventLogSearchService;

    @GetMapping("/events")
    @Operation(summary = "Kafka 이벤트 규칙 목록 조회", description = "플랫폼 전역 Kafka 이벤트 규칙 목록을 조회한다.")
    public ResponseEntity<PageResponse<KafkaEventSummaryResponse>> getEvents(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(PageResponse.from(kafkaMonitoringService.getEvents(pageable)));
    }

    @PostMapping("/events")
    @Operation(summary = "Kafka 이벤트 규칙 생성", description = "플랫폼 전역 Kafka 이벤트 규칙을 생성한다.")
    public ResponseEntity<KafkaEventSummaryResponse> createEventRule(
            @Valid @RequestBody KafkaEventSummaryUpsertRequest request,
            @Parameter(description = "수정 사용자 공개 식별자", example = "usr00000000000000000000001", required = true)
            @RequestHeader("X-User-Public-Id") String actorUserPublicId
    ) {
        return ResponseEntity.ok(kafkaEventRuleService.createRule(request, actorUserPublicId));
    }

    @PutMapping("/events/{ruleId}")
    @Operation(summary = "Kafka 이벤트 규칙 수정", description = "규칙 코드 기준으로 Kafka 이벤트 규칙을 수정한다.")
    public ResponseEntity<KafkaEventSummaryResponse> updateEventRule(
            @PathVariable String ruleId,
            @Valid @RequestBody KafkaEventSummaryUpsertRequest request,
            @Parameter(description = "수정 사용자 공개 식별자", example = "usr00000000000000000000001", required = true)
            @RequestHeader("X-User-Public-Id") String actorUserPublicId
    ) {
        return ResponseEntity.ok(kafkaEventRuleService.updateRule(ruleId, request, actorUserPublicId));
    }

    @PatchMapping("/events/{ruleId}/enabled")
    @Operation(summary = "Kafka 이벤트 규칙 활성화 변경", description = "규칙 코드 기준으로 활성화 여부를 변경한다.")
    public ResponseEntity<KafkaEventSummaryResponse> toggleEventRule(
            @PathVariable String ruleId,
            @Valid @RequestBody KafkaEventRuleToggleRequest request,
            @Parameter(description = "수정 사용자 공개 식별자", example = "usr00000000000000000000001", required = true)
            @RequestHeader("X-User-Public-Id") String actorUserPublicId
    ) {
        return ResponseEntity.ok(kafkaEventRuleService.toggleRule(ruleId, request, actorUserPublicId));
    }

    @DeleteMapping("/events/{ruleId}")
    @Operation(summary = "Kafka 이벤트 규칙 삭제", description = "규칙 코드 기준으로 Kafka 이벤트 규칙을 삭제한다.")
    public ResponseEntity<Void> deleteEventRule(@PathVariable String ruleId) {
        kafkaEventRuleService.deleteRule(ruleId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/subscriptions")
    @Operation(summary = "Kafka 구독 상태 조회", description = "Kafka 토픽 구독 상태 목록을 조회한다.")
    public ResponseEntity<List<KafkaSubscriptionStatusResponse>> getSubscriptions() {
        return ResponseEntity.ok(kafkaMonitoringService.getSubscriptions());
    }

    @GetMapping("/logs")
    @Operation(
            summary = "Kafka 감사로그 검색",
            description = "Kafka 이벤트 발행 성공/실패 감사로그를 Elasticsearch에서 검색합니다."
    )
    public ResponseEntity<PageResponse<EventLogSearchResponse>> getEventLogs(
            EventLogSearchDto searchDto,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(PageResponse.from(eventLogSearchService.search(pageable, searchDto)));
    }

}
