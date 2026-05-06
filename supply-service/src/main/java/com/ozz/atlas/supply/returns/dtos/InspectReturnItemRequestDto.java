package com.ozz.atlas.supply.returns.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "반품 품목 검수 요청")
public class InspectReturnItemRequestDto {
    @Schema(description = "QC 검수 상태 (PASS, FAIL)", example = "PASS")
    @NotBlank
    private String qcStatus;

    @Schema(description = "QC 검수 등급 (A, B, DEFECTIVE)", example = "A")
    @NotBlank
    private String qcGrade;

    @Schema(description = "상세 설명", example = "양품임")
    private String description;
}
