package com.ozz.atlas.supply.returns.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import com.ozz.atlas.supply.returns.domain.ReturnStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "상태 모델")
public class UpdateReturnStatusDto {
    @NotNull(message = "상태 값은 필수입니다.")
    @Schema(description = "상태", example = "ACTIVE")
    private ReturnStatus returnStatus;

    @NotBlank(message = "상태 변경 사유는 필수입니다.")
    @Schema(description = "사유", example = "샘플 내용")
    private String reason;

    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private List<String> attachmentPublicIds;
}