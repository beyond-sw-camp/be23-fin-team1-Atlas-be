package com.ozz.atlas.supply.onboarding.dtos;

import com.ozz.atlas.supply.supplier.domain.SupplierTierLevel;
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
public class CreateOnboardingRequest {

    @NotBlank
    @Size(max = 50)
    private String supplierCode;

    @NotBlank
    @Size(max = 100)
    private String supplierName;

    @NotNull
    private SupplierTierLevel tierLevel;

    @Size(max = 50)
    private String primaryContactName;

    @Email
    @Size(max = 100)
    private String primaryContactEmail;

    @Size(max = 30)
    private String primaryContactPhone;
}
