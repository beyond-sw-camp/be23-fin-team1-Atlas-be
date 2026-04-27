package com.ozz.atlas.control.kafka.rule.service;

import com.ozz.atlas.common.kafka.EventTypes;
import com.ozz.atlas.common.kafka.KafkaTopics;
import com.ozz.atlas.control.kafka.monitoring.dtos.KafkaEventSummaryResponse;
import com.ozz.atlas.control.kafka.monitoring.dtos.KafkaEventRuleToggleRequest;
import com.ozz.atlas.control.kafka.monitoring.dtos.KafkaEventSummaryUpsertRequest;
import com.ozz.atlas.control.kafka.rule.domain.KafkaEventRule;
import com.ozz.atlas.control.kafka.rule.domain.KafkaRuleImportance;
import com.ozz.atlas.control.kafka.rule.exception.KafkaEventRuleErrorCode;
import com.ozz.atlas.control.kafka.rule.exception.KafkaEventRuleException;
import com.ozz.atlas.control.kafka.rule.repository.KafkaEventRuleRepository;
import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KafkaEventRuleService {

    private final KafkaEventRuleRepository kafkaEventRuleRepository;

    @PostConstruct
    @Transactional
    public void initializeDefaultRules() {
        List<KafkaEventRule> missingRules = defaultRules().stream()
                .filter(rule -> !kafkaEventRuleRepository.existsByRuleCode(rule.getRuleCode()))
                .toList();
        if (missingRules.isEmpty()) {
            return;
        }

        kafkaEventRuleRepository.saveAll(missingRules);
    }

    public Page<KafkaEventSummaryResponse> getRules(Pageable pageable) {
        return kafkaEventRuleRepository.findAllByOrderByRuleCodeAsc(pageable).map(this::toResponse);
    }

    @Transactional
    public KafkaEventSummaryResponse createRule(KafkaEventSummaryUpsertRequest request, String actorUserPublicId) {
        validateDuplicateRuleCode(request.getRuleId(), null);

        KafkaEventRule rule = KafkaEventRule.create(
                request.getRuleId(),
                request.getRuleName(),
                request.getTopic(),
                request.getEventType(),
                request.getCondition(),
                request.getThreshold(),
                request.getImportance(),
                request.isEnabled(),
                actorUserPublicId
        );

        return toResponse(kafkaEventRuleRepository.save(rule));
    }

    @Transactional
    public KafkaEventSummaryResponse updateRule(
            String ruleCode,
            KafkaEventSummaryUpsertRequest request,
            String actorUserPublicId
    ) {
        KafkaEventRule rule = getRuleEntity(ruleCode);
        validateDuplicateRuleCode(request.getRuleId(), ruleCode);

        rule.update(
                request.getRuleName(),
                request.getTopic(),
                request.getEventType(),
                request.getCondition(),
                request.getThreshold(),
                request.getImportance(),
                request.isEnabled(),
                actorUserPublicId
        );

        return toResponse(rule);
    }

    @Transactional
    public KafkaEventSummaryResponse toggleRule(
            String ruleCode,
            KafkaEventRuleToggleRequest request,
            String actorUserPublicId
    ) {
        KafkaEventRule rule = getRuleEntity(ruleCode);
        rule.changeEnabled(Boolean.TRUE.equals(request.getEnabled()), actorUserPublicId);
        return toResponse(rule);
    }

    @Transactional
    public void deleteRule(String ruleCode) {
        KafkaEventRule rule = getRuleEntity(ruleCode);
        kafkaEventRuleRepository.delete(rule);
    }

    public boolean isEnabled(String eventType) {
        return kafkaEventRuleRepository.existsByEventTypeAndEnabledTrue(eventType);
    }

    @Transactional
    public void markTriggered(String eventType) {
        kafkaEventRuleRepository.findAllByEventType(eventType).stream()
                .filter(KafkaEventRule::isEnabled)
                .forEach(KafkaEventRule::increaseTriggeredCount);
    }

    private KafkaEventRule getRuleEntity(String ruleCode) {
        return kafkaEventRuleRepository.findByRuleCode(ruleCode)
                .orElseThrow(() -> new KafkaEventRuleException(KafkaEventRuleErrorCode.RULE_NOT_FOUND));
    }

    private void validateDuplicateRuleCode(String ruleCode, String currentRuleCode) {
        boolean duplicatedRuleCode = currentRuleCode == null
                ? kafkaEventRuleRepository.existsByRuleCode(ruleCode)
                : kafkaEventRuleRepository.existsByRuleCodeAndRuleCodeNot(ruleCode, currentRuleCode);
        if (duplicatedRuleCode) {
            throw new KafkaEventRuleException(KafkaEventRuleErrorCode.DUPLICATE_RULE_CODE);
        }
    }

    private KafkaEventSummaryResponse toResponse(KafkaEventRule rule) {
        return KafkaEventSummaryResponse.builder()
                .ruleId(rule.getRuleCode())
                .ruleName(rule.getRuleName())
                .topic(rule.getTopic())
                .condition(rule.getCondition())
                .threshold(rule.getThreshold())
                .triggeredCount((int) rule.getTriggeredCount())
                .enabled(rule.isEnabled())
                .importance(rule.getImportance().name())
                .eventType(rule.getEventType())
                .build();
    }

    private List<KafkaEventRule> defaultRules() {
        return List.of(
                rule("PO-001", "발주 생성", KafkaTopics.SUPPLY_PURCHASE_ORDER, EventTypes.PURCHASE_ORDER_CREATED, "이벤트 발생", "발주 생성 시", KafkaRuleImportance.MEDIUM),
                rule("PO-002", "발주 수정", KafkaTopics.SUPPLY_PURCHASE_ORDER, EventTypes.PURCHASE_ORDER_UPDATED, "이벤트 발생", "발주 수정 시", KafkaRuleImportance.MEDIUM),
                rule("PO-003", "발주 확정", KafkaTopics.SUPPLY_PURCHASE_ORDER, EventTypes.PURCHASE_ORDER_CONFIRMED, "이벤트 발생", "발주 확정 시", KafkaRuleImportance.MEDIUM),
                rule("PO-004", "발주 수락", KafkaTopics.SUPPLY_PURCHASE_ORDER, EventTypes.PURCHASE_ORDER_ACCEPTED, "이벤트 발생", "발주 수락 시", KafkaRuleImportance.MEDIUM),
                rule("PO-005", "발주 거절", KafkaTopics.SUPPLY_PURCHASE_ORDER, EventTypes.PURCHASE_ORDER_REJECTED, "이벤트 발생", "발주 거절 시", KafkaRuleImportance.HIGH),
                rule("PO-006", "발주 취소", KafkaTopics.SUPPLY_PURCHASE_ORDER, EventTypes.PURCHASE_ORDER_CANCELLED, "이벤트 발생", "발주 취소 시", KafkaRuleImportance.HIGH),

                rule("SUB-001", "하위 발주 생성", KafkaTopics.SUPPLY_SUB_PURCHASE_ORDER, EventTypes.SUB_PURCHASE_ORDER_CREATED, "이벤트 발생", "하위 발주 생성 시", KafkaRuleImportance.MEDIUM),
                rule("SUB-002", "하위 발주 확정", KafkaTopics.SUPPLY_SUB_PURCHASE_ORDER, EventTypes.SUB_PURCHASE_ORDER_CONFIRMED, "이벤트 발생", "하위 발주 확정 시", KafkaRuleImportance.MEDIUM),
                rule("SUB-003", "하위 발주 거절", KafkaTopics.SUPPLY_SUB_PURCHASE_ORDER, EventTypes.SUB_PURCHASE_ORDER_REJECTED, "이벤트 발생", "하위 발주 거절 시", KafkaRuleImportance.HIGH),
                rule("SUB-004", "하위 발주 취소", KafkaTopics.SUPPLY_SUB_PURCHASE_ORDER, EventTypes.SUB_PURCHASE_ORDER_CANCELLED, "이벤트 발생", "하위 발주 취소 시", KafkaRuleImportance.HIGH),

                rule("SHP-001", "출하 생성", KafkaTopics.SUPPLY_SHIPMENT, EventTypes.SHIPMENT_CREATED, "이벤트 발생", "출하 생성 시", KafkaRuleImportance.MEDIUM),
                rule("SHP-002", "출하 출발", KafkaTopics.SUPPLY_SHIPMENT, EventTypes.SHIPMENT_DEPARTED, "이벤트 발생", "출하 출발 시", KafkaRuleImportance.LOW),
                rule("SHP-003", "출하 도착", KafkaTopics.SUPPLY_SHIPMENT, EventTypes.SHIPMENT_ARRIVED, "이벤트 발생", "출하 도착 시", KafkaRuleImportance.LOW),
                rule("SHP-004", "출하 완료", KafkaTopics.SUPPLY_SHIPMENT, EventTypes.SHIPMENT_COMPLETED, "이벤트 발생", "출하 완료 시", KafkaRuleImportance.LOW),
                rule("SHP-005", "출하 지연 감지", KafkaTopics.SUPPLY_SHIPMENT, EventTypes.SHIPMENT_DELAY_DETECTED, "지연 시간 >=", "출하 지연 감지 시", KafkaRuleImportance.HIGH),
                rule("SHP-006", "연속 납기 실패 감지", KafkaTopics.SUPPLY_SHIPMENT, EventTypes.SHIPMENT_DELAY_DETECTED, "연속 실패 >=", "3회", KafkaRuleImportance.CRITICAL),

                rule("DEL-001", "배송 예외 생성", KafkaTopics.SUPPLY_DELIVERY_EXCEPTION, EventTypes.DELIVERY_EXCEPTION_CREATED, "이벤트 발생", "배송 예외 생성 시", KafkaRuleImportance.MEDIUM),
                rule("DEL-002", "배송 지연 예외", KafkaTopics.SUPPLY_DELIVERY_EXCEPTION, EventTypes.DELIVERY_EXCEPTION_DELAY, "이벤트 발생", "배송 지연 예외 발생 시", KafkaRuleImportance.HIGH),
                rule("DEL-003", "온도 이탈 예외", KafkaTopics.SUPPLY_DELIVERY_EXCEPTION, EventTypes.DELIVERY_EXCEPTION_TEMPERATURE_DEVIATION, "이벤트 발생", "온도 이탈 예외 발생 시", KafkaRuleImportance.CRITICAL),
                rule("DEL-004", "파손 예외", KafkaTopics.SUPPLY_DELIVERY_EXCEPTION, EventTypes.DELIVERY_EXCEPTION_DAMAGED, "이벤트 발생", "파손 예외 발생 시", KafkaRuleImportance.CRITICAL),

                rule("LOG-001", "창고 용량 초과", KafkaTopics.SUPPLY_LOGISTICS_NODE, EventTypes.LOGISTICS_NODE_CAPACITY_STATUS_CHANGED, "용량 상태 =", "FULL", KafkaRuleImportance.HIGH),

                rule("INV-001", "재고 부족 경보", KafkaTopics.SUPPLY_INVENTORY, EventTypes.INVENTORY_SHORTAGE_DETECTED, "재고 상태 =", "SHORTAGE", KafkaRuleImportance.HIGH),

                rule("LOT-001", "LOT 생성", KafkaTopics.SUPPLY_LOT, EventTypes.LOT_CREATED, "이벤트 발생", "LOT 생성 시", KafkaRuleImportance.LOW),
                rule("LOT-002", "LOT 생산 중", KafkaTopics.SUPPLY_LOT, EventTypes.LOT_IN_PRODUCTION, "이벤트 발생", "LOT 생산 시작 시", KafkaRuleImportance.LOW),
                rule("LOT-003", "LOT 완료", KafkaTopics.SUPPLY_LOT, EventTypes.LOT_COMPLETED, "이벤트 발생", "LOT 완료 시", KafkaRuleImportance.LOW),
                rule("LOT-004", "LOT 보류", KafkaTopics.SUPPLY_LOT, EventTypes.LOT_HOLD, "이벤트 발생", "LOT 보류 시", KafkaRuleImportance.HIGH),
                rule("LOT-005", "LOT 보류 해제", KafkaTopics.SUPPLY_LOT, EventTypes.LOT_RELEASED, "이벤트 발생", "LOT 보류 해제 시", KafkaRuleImportance.MEDIUM),
                rule("LOT-006", "LOT 불량", KafkaTopics.SUPPLY_LOT, EventTypes.LOT_DEFECTIVE, "이벤트 발생", "LOT 불량 판정 시", KafkaRuleImportance.CRITICAL),
                rule("LOT-007", "LOT 유통기한 만료 임박", KafkaTopics.SUPPLY_LOT, EventTypes.LOT_EXPIRATION_IMMINENT, "남은 일수 <=", "7일", KafkaRuleImportance.MEDIUM),
                rule("LOT-008", "LOT 품질 통과", KafkaTopics.SUPPLY_LOT, EventTypes.LOT_QUALITY_PASSED, "이벤트 발생", "LOT 품질 검사 통과 시", KafkaRuleImportance.LOW),
                rule("LOT-009", "LOT 품질 실패", KafkaTopics.SUPPLY_LOT, EventTypes.LOT_QUALITY_FAILED, "이벤트 발생", "LOT 품질 검사 실패 시", KafkaRuleImportance.CRITICAL),

                rule("RTR-001", "반품 요청 생성", KafkaTopics.SUPPLY_RETURN_REQUEST, EventTypes.RETURN_REQUEST_CREATED, "이벤트 발생", "반품 요청 생성 시", KafkaRuleImportance.MEDIUM),
                rule("RTR-002", "반품 요청 승인", KafkaTopics.SUPPLY_RETURN_REQUEST, EventTypes.RETURN_REQUEST_APPROVED, "이벤트 발생", "반품 요청 승인 시", KafkaRuleImportance.MEDIUM),
                rule("RTR-003", "반품 요청 거절", KafkaTopics.SUPPLY_RETURN_REQUEST, EventTypes.RETURN_REQUEST_REJECTED, "이벤트 발생", "반품 요청 거절 시", KafkaRuleImportance.HIGH),
                rule("RTR-004", "반품 요청 완료", KafkaTopics.SUPPLY_RETURN_REQUEST, EventTypes.RETURN_REQUEST_COMPLETED, "이벤트 발생", "반품 완료 시", KafkaRuleImportance.LOW),
                rule("RTR-005", "반품 요청 취소", KafkaTopics.SUPPLY_RETURN_REQUEST, EventTypes.RETURN_REQUEST_CANCELLED, "이벤트 발생", "반품 요청 취소 시", KafkaRuleImportance.MEDIUM),

                rule("CERT-001", "협력사 인증서 생성", KafkaTopics.SUPPLY_SUPPLIER_CERTIFICATE, EventTypes.SUPPLIER_CERTIFICATE_CREATED, "이벤트 발생", "협력사 인증서 생성 시", KafkaRuleImportance.MEDIUM),
                rule("CERT-002", "협력사 인증서 승인", KafkaTopics.SUPPLY_SUPPLIER_CERTIFICATE, EventTypes.SUPPLIER_CERTIFICATE_APPROVED, "이벤트 발생", "협력사 인증서 승인 시", KafkaRuleImportance.MEDIUM),
                rule("CERT-003", "협력사 인증서 거절", KafkaTopics.SUPPLY_SUPPLIER_CERTIFICATE, EventTypes.SUPPLIER_CERTIFICATE_REJECTED, "이벤트 발생", "협력사 인증서 거절 시", KafkaRuleImportance.HIGH),
                rule("CERT-004", "협력사 인증서 만료 임박", KafkaTopics.SUPPLY_SUPPLIER_CERTIFICATE, EventTypes.SUPPLIER_CERTIFICATE_EXPIRING, "이벤트 발생", "협력사 인증서 만료 임박 시", KafkaRuleImportance.HIGH),
                rule("CERT-005", "협력사 인증서 만료", KafkaTopics.SUPPLY_SUPPLIER_CERTIFICATE, EventTypes.SUPPLIER_CERTIFICATE_EXPIRED, "이벤트 발생", "협력사 인증서 만료 시", KafkaRuleImportance.CRITICAL),
                rule("CERT-006", "협력사 인증서 철회", KafkaTopics.SUPPLY_SUPPLIER_CERTIFICATE, EventTypes.SUPPLIER_CERTIFICATE_REVOKED, "이벤트 발생", "협력사 인증서 철회 시", KafkaRuleImportance.HIGH),
                rule("CERT-007", "ESG 위반 알림", KafkaTopics.SUPPLY_SUPPLIER_CERTIFICATE, EventTypes.SUPPLIER_CERTIFICATE_EXPIRED, "인증 상태 =", "만료", KafkaRuleImportance.CRITICAL),

                rule("SUP-001", "협력사 점수 급락 감지", KafkaTopics.SUPPLY_SUPPLIER_RISK, EventTypes.SUPPLIER_SCORE_DROPPED, "점수 변화 <=", "-10pt", KafkaRuleImportance.HIGH),
                rule("SUP-002", "협력사 ESG 위반", KafkaTopics.SUPPLY_SUPPLIER_RISK, EventTypes.SUPPLIER_ESG_VIOLATED, "ESG 상태 =", "VIOLATED", KafkaRuleImportance.CRITICAL),

                rule("REC-001", "권고안 생성 요청", KafkaTopics.CONTROL_RECOMMENDATION_REQUESTED, EventTypes.RECOMMENDATION_REQUESTED, "이벤트 발생", "권고안 생성 요청 시", KafkaRuleImportance.MEDIUM),
                rule("REC-002", "권고안 생성 완료", KafkaTopics.CONTROL_RECOMMENDATION_GENERATED, EventTypes.RECOMMENDATION_GENERATED, "이벤트 발생", "권고안 생성 성공 시", KafkaRuleImportance.MEDIUM),
                rule("REC-003", "권고안 생성 실패", KafkaTopics.CONTROL_RECOMMENDATION_FAILED, EventTypes.RECOMMENDATION_FAILED, "이벤트 발생", "권고안 생성 실패 시", KafkaRuleImportance.CRITICAL),
                rule("REC-004", "권고안 수락", KafkaTopics.CONTROL_RECOMMENDATION_DECISION, EventTypes.RECOMMENDATION_ACCEPTED, "이벤트 발생", "권고안 수락 시", KafkaRuleImportance.MEDIUM),
                rule("REC-005", "권고안 거절", KafkaTopics.CONTROL_RECOMMENDATION_DECISION, EventTypes.RECOMMENDATION_REJECTED, "이벤트 발생", "권고안 거절 시", KafkaRuleImportance.MEDIUM)
        );
    }

    private KafkaEventRule rule(
            String ruleCode,
            String ruleName,
            String topic,
            String eventType,
            String condition,
            String threshold,
            KafkaRuleImportance importance
    ) {
        return KafkaEventRule.create(
                ruleCode,
                ruleName,
                topic,
                eventType,
                condition,
                threshold,
                importance,
                true,
                null
        );
    }
}
