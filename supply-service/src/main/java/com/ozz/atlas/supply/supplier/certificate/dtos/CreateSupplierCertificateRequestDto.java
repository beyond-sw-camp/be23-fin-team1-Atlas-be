package com.ozz.atlas.supply.supplier.certificate.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Create Supplier Certificate 값 요청")
public class CreateSupplierCertificateRequestDto {
    @NotBlank(message = "인증 유형 ID는 필수입니다.")
    @Schema(description = "공개 식별자", example = "sample_public_id")
    private String certificateTypePublicId;
    @Schema(description = "certificate No 값", example = "NO-2026-0001", nullable = true)
    private String certificateNo;
    @Schema(description = "issued At 값", example = "2026-05-08T10:00:00", nullable = true)
    private LocalDate issuedAt;
    @Schema(description = "expired At 값", example = "2026-05-08T10:00:00", nullable = true)
    private LocalDate expiredAt;
    @Schema(description = "이름", example = "샘플 이름", nullable = true)
    private String issuerName;
    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String attachmentPublicId;
}