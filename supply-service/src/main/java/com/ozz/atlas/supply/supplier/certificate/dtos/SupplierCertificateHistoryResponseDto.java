package com.ozz.atlas.supply.supplier.certificate.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import com.ozz.atlas.supply.supplier.certificate.domain.CertificateStatus;
import com.ozz.atlas.supply.supplier.certificate.domain.SupplierCertificateHistory;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "Supplier Certificate History 값 응답")
public class SupplierCertificateHistoryResponseDto {
    @Schema(description = "유형", example = "DEFAULT", nullable = true)
    private String actionType;
    @Schema(description = "상태", example = "ACTIVE", nullable = true)
    private CertificateStatus beforeStatus;
    @Schema(description = "상태", example = "ACTIVE", nullable = true)
    private CertificateStatus afterStatus;
    @Schema(description = "사유", example = "샘플 내용", nullable = true)
    private String reason;
    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String actorPublicId;
    @Schema(description = "recorded At 값", example = "2026-05-08T10:00:00", nullable = true)
    private LocalDateTime recordedAt;
    public static SupplierCertificateHistoryResponseDto from(SupplierCertificateHistory entity) {
        return SupplierCertificateHistoryResponseDto.builder()
                .actionType(entity.getActionType())
                .beforeStatus(entity.getBeforeStatus())
                .afterStatus(entity.getAfterStatus())
                .reason(entity.getReason())
                .actorPublicId(entity.getActorPublicId())
                .recordedAt(entity.getRecordedAt())
                .build();
    }
}