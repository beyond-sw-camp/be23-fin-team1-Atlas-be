package com.ozz.atlas.supply.supplier.certificate.dtos;

import com.ozz.atlas.supply.supplier.certificate.domain.CertificateStatus;
import com.ozz.atlas.supply.supplier.certificate.domain.SupplierCertificateHistory;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SupplierCertificateHistoryResponseDto {
    private String actionType;
    private CertificateStatus beforeStatus;
    private CertificateStatus afterStatus;
    private String reason;
    private String actorPublicId;
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