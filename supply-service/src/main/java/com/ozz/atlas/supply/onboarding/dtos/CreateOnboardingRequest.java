package com.ozz.atlas.supply.onboarding.dtos;

import com.ozz.atlas.supply.supplier.domain.SupplierTierLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "협력사 온보딩 생성 요청")
public class CreateOnboardingRequest {

    @NotBlank
    @Size(max = 50)
    @Schema(description = "협력사 내부 코드", example = "SUP-FOOD-001")
    private String supplierCode;

    @NotBlank
    @Size(max = 100)
    @Schema(description = "협력사명", example = "Fresh Chain Co.")
    private String supplierName;

    @NotNull
    @Schema(description = "협력사 tier 수준", example = "TIER1")
    private SupplierTierLevel tierLevel;

    @Size(max = 50)
    @Schema(description = "주요 담당자 이름", example = "Park Jisoo")
    private String primaryContactName;

    @Email
    @Size(max = 100)
    @Schema(description = "주요 담당자 이메일", example = "partner@freshchain.com")
    private String primaryContactEmail;

    @Size(max = 30)
    @Schema(description = "주요 담당자 연락처", example = "02-3456-7890")
    private String primaryContactPhone;
}
