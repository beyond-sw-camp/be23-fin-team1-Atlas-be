package com.ozz.atlas.supply.supplier.esg.controller;

import com.ozz.atlas.supply.supplier.esg.dtos.CreateSupplyEsgAssessmentRequest;
import com.ozz.atlas.supply.supplier.esg.dtos.UpdateSupplyEsgAssessmentRequest;
import com.ozz.atlas.supply.supplier.esg.service.SupplyEsgAssessmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/esg")
@Tag(name = "SupplyEsgAssessment")
public class SupplyEsgAssessmentController {

    private final SupplyEsgAssessmentService supplyEsgAssessmentService;

    @Operation(summary = "협력사 ESG 평가 생성", description = "협력사 ESG 평가를 생성하고 점수 급락 또는 E등급이면 공급사 리스크 이벤트를 발행 대기 상태로 적재합니다.")
    @PostMapping("/suppliers/{supplierPublicId}/assessments")
    public ResponseEntity<?> createAssessment(
            @PathVariable String supplierPublicId,
            @RequestHeader(value = "X-User-Public-Id", required = false) String actorUserPublicId,
            @Valid @RequestBody CreateSupplyEsgAssessmentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(supplyEsgAssessmentService.createAssessment(
                        supplierPublicId,
                        request,
                        actorUserPublicId
                ));
    }

    @Operation(summary = "협력사 ESG 평가 목록 조회")
    @GetMapping("/suppliers/{supplierPublicId}/assessments")
    public ResponseEntity<?> getAssessments(
            @PathVariable String supplierPublicId,
            @ParameterObject Pageable pageable
    ) {
        return ResponseEntity.ok(supplyEsgAssessmentService.getAssessments(supplierPublicId, pageable));
    }

    @Operation(summary = "협력사 최신 ESG 평가 조회")
    @GetMapping("/suppliers/{supplierPublicId}/assessments/latest")
    public ResponseEntity<?> getLatestAssessment(@PathVariable String supplierPublicId) {
        return ResponseEntity.ok(supplyEsgAssessmentService.getLatestAssessment(supplierPublicId));
    }

    @Operation(summary = "협력사 최신 ESG 점수 조회")
    @GetMapping("/suppliers/{supplierPublicId}/score")
    public ResponseEntity<?> getLatestScore(@PathVariable String supplierPublicId) {
        return ResponseEntity.ok(supplyEsgAssessmentService.getLatestScore(supplierPublicId));
    }

    @Operation(summary = "ESG 평가 상세 조회")
    @GetMapping("/assessments/{esgAssessmentId}")
    public ResponseEntity<?> getAssessment(
            @Parameter(description = "ESG 평가 ID") @PathVariable Long esgAssessmentId
    ) {
        return ResponseEntity.ok(supplyEsgAssessmentService.getAssessment(esgAssessmentId));
    }

    @Operation(summary = "ESG 평가 수정", description = "ESG 평가를 수정하고 점수 급락 또는 E등급이면 공급사 리스크 이벤트를 발행 대기 상태로 적재합니다.")
    @PatchMapping("/assessments/{esgAssessmentId}")
    public ResponseEntity<?> updateAssessment(
            @Parameter(description = "ESG 평가 ID") @PathVariable Long esgAssessmentId,
            @RequestHeader(value = "X-User-Public-Id", required = false) String actorUserPublicId,
            @Valid @RequestBody UpdateSupplyEsgAssessmentRequest request
    ) {
        return ResponseEntity.ok(supplyEsgAssessmentService.updateAssessment(
                esgAssessmentId,
                request,
                actorUserPublicId
        ));
    }

    @Operation(summary = "협력사 ESG 랭킹 조회", description = "협력사별 최신 ESG 평가 기준 랭킹을 조회합니다.")
    @GetMapping("/rankings")
    public ResponseEntity<?> getRankings(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(supplyEsgAssessmentService.getRankings(pageable));
    }
}
