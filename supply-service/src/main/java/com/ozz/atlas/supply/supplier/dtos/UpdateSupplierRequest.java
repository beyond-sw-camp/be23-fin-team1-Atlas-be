package com.ozz.atlas.supply.supplier.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "날짜 요청")
public class UpdateSupplierRequest {

    @NotBlank
    @Size(max = 50)
    @Schema(description = "코드", example = "CODE-001")
    private String supplierCode;

    @NotBlank
    @Size(max = 100)
    @Schema(description = "이름", example = "샘플 이름")
    private String supplierName;

    @Size(max = 50)
    @Schema(description = "이름", example = "샘플 이름", nullable = true)
    private String primaryContactName;

    @Email
    @Size(max = 100)
    @Schema(description = "이메일", example = "user@atlas.com", nullable = true)
    private String primaryContactEmail;

    @Size(max = 30)
    @Schema(description = "연락처", example = "010-1234-5678", nullable = true)
    private String primaryContactPhone;
}
