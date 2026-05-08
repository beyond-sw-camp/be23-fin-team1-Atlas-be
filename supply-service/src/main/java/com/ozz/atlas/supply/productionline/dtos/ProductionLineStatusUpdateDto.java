package com.ozz.atlas.supply.productionline.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import com.ozz.atlas.common.jpa.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "상태 모델")
public class ProductionLineStatusUpdateDto {

    @NotNull
    @Schema(description = "상태", example = "ACTIVE")
    private Status status;
}
