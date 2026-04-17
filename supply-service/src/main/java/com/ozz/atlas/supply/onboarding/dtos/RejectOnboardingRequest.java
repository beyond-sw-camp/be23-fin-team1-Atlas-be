package com.ozz.atlas.supply.onboarding.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "온보딩 반려 요청")
public class RejectOnboardingRequest {

    @NotBlank
    @Schema(description = "반려 사유", example = "필수 인증 서류가 누락되었습니다.")
    private String rejectReason;
}
