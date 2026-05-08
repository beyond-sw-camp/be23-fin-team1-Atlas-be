package com.ozz.atlas.supply.returns.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import com.ozz.atlas.supply.returns.domain.ResolutionType;
import com.ozz.atlas.supply.returns.domain.ReturnType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "날짜 요청")
public class UpdateReturnRequestDto {
    @Schema(description = "유형", example = "DEFAULT", nullable = true)
    private ReturnType returnType;
    @Schema(description = "유형", example = "DEFAULT", nullable = true)
    private ResolutionType resolutionType;
    @Schema(description = "사유", example = "샘플 내용", nullable = true)
    private String returnReason;
    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private List<String> attachmentPublicIds;
}