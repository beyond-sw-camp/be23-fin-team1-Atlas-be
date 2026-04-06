package com.ozz.atlas.supply.onboarding.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RejectOnboardingRequest {

    @NotBlank
    private String rejectReason;
}
