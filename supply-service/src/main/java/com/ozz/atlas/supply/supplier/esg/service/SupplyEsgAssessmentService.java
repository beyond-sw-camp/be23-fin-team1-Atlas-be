package com.ozz.atlas.supply.supplier.esg.service;

import com.ozz.atlas.common.kafka.AggregateType;
import com.ozz.atlas.common.kafka.EventTypes;
import com.ozz.atlas.common.kafka.KafkaTopics;
import com.ozz.atlas.supply.kafka.context.SupplyChainContext;
import com.ozz.atlas.supply.kafka.context.SupplyChainContextResolver;
import com.ozz.atlas.supply.kafka.event.SupplyDomainEventFactory;
import com.ozz.atlas.supply.kafka.event.SupplyDomainEventPayload;
import com.ozz.atlas.supply.kafka.outbox.OutboxEventAppender;
import com.ozz.atlas.supply.supplier.domain.SupplierStatus;
import com.ozz.atlas.supply.supplier.domain.SupplySupplier;
import com.ozz.atlas.supply.supplier.esg.domain.EsgGrade;
import com.ozz.atlas.supply.supplier.esg.domain.SupplyEsgAssessment;
import com.ozz.atlas.supply.supplier.esg.dtos.CreateSupplyEsgAssessmentRequest;
import com.ozz.atlas.supply.supplier.esg.dtos.SupplyEsgAssessmentResponse;
import com.ozz.atlas.supply.supplier.esg.dtos.SupplyEsgRankingResponse;
import com.ozz.atlas.supply.supplier.esg.dtos.SupplyEsgScoreResponse;
import com.ozz.atlas.supply.supplier.esg.dtos.UpdateSupplyEsgAssessmentRequest;
import com.ozz.atlas.supply.supplier.esg.exception.EsgAssessmentErrorCode;
import com.ozz.atlas.supply.supplier.esg.exception.EsgAssessmentException;
import com.ozz.atlas.supply.supplier.esg.repository.SupplyEsgAssessmentRepository;
import com.ozz.atlas.supply.supplier.repository.SupplierRepository;
import com.ozz.atlas.supply.supplier.search.service.SupplierSearchService;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SupplyEsgAssessmentService {

    private static final BigDecimal SCORE_DROP_THRESHOLD = new BigDecimal("10.00");

    private final SupplyEsgAssessmentRepository esgAssessmentRepository;
    private final SupplierRepository supplierRepository;
    private final SupplierSearchService supplierSearchService;
    private final OutboxEventAppender outboxEventAppender;
    private final SupplyDomainEventFactory supplyDomainEventFactory;
    private final SupplyChainContextResolver supplyChainContextResolver;

    public SupplyEsgAssessmentResponse createAssessment(
            String supplierPublicId,
            CreateSupplyEsgAssessmentRequest request,
            String actorUserPublicId
    ) {
        SupplySupplier supplier = getSupplierOrThrow(supplierPublicId);
        SupplyEsgAssessment latestAssessment = esgAssessmentRepository
                .findTopBySupplier_IdOrderByEvaluatedAtDesc(supplier.getId())
                .orElse(null);

        SupplyEsgAssessment assessment = SupplyEsgAssessment.create(
                supplier,
                request.getEnvironmentScore(),
                request.getSocialScore(),
                request.getGovernanceScore(),
                request.getEvaluatorName(),
                request.getNote()
        );

        SupplyEsgAssessment savedAssessment = esgAssessmentRepository.saveAndFlush(assessment);
        supplierSearchService.saveSupplierDocument(supplier);
        appendSupplierRiskEvents(savedAssessment, latestAssessment, actorUserPublicId);

        return SupplyEsgAssessmentResponse.fromEntity(savedAssessment);
    }

    public SupplyEsgAssessmentResponse updateAssessment(
            Long esgAssessmentId,
            UpdateSupplyEsgAssessmentRequest request,
            String actorUserPublicId
    ) {
        if (isEmptyPatch(request)) {
            throw new EsgAssessmentException(EsgAssessmentErrorCode.ESG_ASSESSMENT_EMPTY_PATCH);
        }

        SupplyEsgAssessment assessment = esgAssessmentRepository.findById(esgAssessmentId)
                .orElseThrow(() -> new EsgAssessmentException(EsgAssessmentErrorCode.ESG_ASSESSMENT_NOT_FOUND));
        SupplyEsgAssessment previousSnapshot = snapshot(assessment);

        assessment.update(
                request.getEnvironmentScore(),
                request.getSocialScore(),
                request.getGovernanceScore(),
                request.getEvaluatorName(),
                request.getNote()
        );

        supplierSearchService.saveSupplierDocument(assessment.getSupplier());
        appendSupplierRiskEvents(assessment, previousSnapshot, actorUserPublicId);

        return SupplyEsgAssessmentResponse.fromEntity(assessment);
    }

    @Transactional(readOnly = true)
    public SupplyEsgAssessmentResponse getAssessment(Long esgAssessmentId) {
        return SupplyEsgAssessmentResponse.fromEntity(
                esgAssessmentRepository.findById(esgAssessmentId)
                        .orElseThrow(() -> new EsgAssessmentException(EsgAssessmentErrorCode.ESG_ASSESSMENT_NOT_FOUND))
        );
    }

    @Transactional(readOnly = true)
    public SupplyEsgAssessmentResponse getLatestAssessment(String supplierPublicId) {
        SupplySupplier supplier = getSupplierOrThrow(supplierPublicId);
        SupplyEsgAssessment assessment = esgAssessmentRepository
                .findTopBySupplier_IdOrderByEvaluatedAtDesc(supplier.getId())
                .orElseThrow(() -> new EsgAssessmentException(EsgAssessmentErrorCode.ESG_ASSESSMENT_NOT_FOUND));

        return SupplyEsgAssessmentResponse.fromEntity(assessment);
    }

    @Transactional(readOnly = true)
    public SupplyEsgScoreResponse getLatestScore(String supplierPublicId) {
        SupplySupplier supplier = getSupplierOrThrow(supplierPublicId);
        SupplyEsgAssessment assessment = esgAssessmentRepository
                .findTopBySupplier_IdOrderByEvaluatedAtDesc(supplier.getId())
                .orElseThrow(() -> new EsgAssessmentException(EsgAssessmentErrorCode.ESG_ASSESSMENT_NOT_FOUND));

        return SupplyEsgScoreResponse.fromEntity(assessment);
    }

    @Transactional(readOnly = true)
    public Page<SupplyEsgAssessmentResponse> getAssessments(String supplierPublicId, Pageable pageable) {
        SupplySupplier supplier = getSupplierOrThrow(supplierPublicId);

        return esgAssessmentRepository.findAllBySupplier_IdOrderByEvaluatedAtDesc(supplier.getId(), pageable)
                .map(SupplyEsgAssessmentResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<SupplyEsgRankingResponse> getRankings(Pageable pageable) {
        int startRank = pageable.isPaged() ? (int) pageable.getOffset() + 1 : 1;
        AtomicInteger rank = new AtomicInteger(startRank);
        return esgAssessmentRepository.findLatestAssessmentsBySupplier(pageable)
                .map(assessment -> SupplyEsgRankingResponse.fromEntity(
                        rank.getAndIncrement(),
                        assessment
                ));
    }

    private SupplySupplier getSupplierOrThrow(String supplierPublicId) {
        return supplierRepository.findByPublicIdAndSupplierStatusNot(
                        supplierPublicId,
                        SupplierStatus.TERMINATED
                )
                .orElseThrow(() -> new EsgAssessmentException(EsgAssessmentErrorCode.SUPPLIER_NOT_FOUND));
    }

    private boolean isEmptyPatch(UpdateSupplyEsgAssessmentRequest request) {
        return request.getEnvironmentScore() == null
                && request.getSocialScore() == null
                && request.getGovernanceScore() == null
                && request.getEvaluatorName() == null
                && request.getNote() == null;
    }

    private void appendSupplierRiskEvents(
            SupplyEsgAssessment assessment,
            SupplyEsgAssessment previousAssessment,
            String actorUserPublicId
    ) {
        if (previousAssessment != null
                && previousAssessment.getTotalScore()
                .subtract(assessment.getTotalScore())
                .compareTo(SCORE_DROP_THRESHOLD) >= 0) {
            appendSupplierRiskEvent(
                    assessment,
                    EventTypes.SUPPLIER_SCORE_DROPPED,
                    "협력사 점수 급락 감지",
                    "협력사 ESG 점수가 %s점에서 %s점으로 하락했습니다.".formatted(
                            previousAssessment.getTotalScore(),
                            assessment.getTotalScore()
                    ),
                    actorUserPublicId
            );
        }

        if (isNewEsgViolation(assessment, previousAssessment)) {
            appendSupplierRiskEvent(
                    assessment,
                    EventTypes.SUPPLIER_ESG_VIOLATED,
                    "협력사 ESG 위반 알림",
                    "협력사 ESG 등급이 E등급입니다.",
                    actorUserPublicId
            );
        }
    }

    private boolean isNewEsgViolation(
            SupplyEsgAssessment assessment,
            SupplyEsgAssessment previousAssessment
    ) {
        return EsgGrade.E.equals(assessment.getGrade())
                && (previousAssessment == null || !EsgGrade.E.equals(previousAssessment.getGrade()));
    }

    private void appendSupplierRiskEvent(
            SupplyEsgAssessment assessment,
            String eventType,
            String eventName,
            String description,
            String actorUserPublicId
    ) {
        SupplySupplier supplier = assessment.getSupplier();
        SupplyChainContext context = supplyChainContextResolver.fromSupplier(supplier);
        SupplyDomainEventPayload payload = supplyDomainEventFactory.payload(
                supplier.getPublicId(),
                supplier.getSupplierCode(),
                assessment.getGrade().name(),
                eventName,
                description,
                null
        );

        outboxEventAppender.append(supplyDomainEventFactory.create(
                KafkaTopics.SUPPLY_SUPPLIER_RISK,
                eventType,
                AggregateType.RISK,
                supplier.getPublicId(),
                actorUserPublicId,
                supplier.getOrganizationPublicId(),
                context,
                payload
        ));
    }

    private SupplyEsgAssessment snapshot(SupplyEsgAssessment assessment) {
        return SupplyEsgAssessment.create(
                assessment.getSupplier(),
                assessment.getEnvironmentScore(),
                assessment.getSocialScore(),
                assessment.getGovernanceScore(),
                assessment.getEvaluatorName(),
                assessment.getNote()
        );
    }
}
