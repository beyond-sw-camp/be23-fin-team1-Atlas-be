package com.ozz.atlas.supply.supplier.certificate.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Reject Certificate 값 요청")
public class RejectCertificateRequestDto {
    @NotBlank(message = "반려 사유는 필수입니다.")
    @Schema(description = "사유", example = "샘플 내용")
    private String rejectReason;
}