package com.ozz.atlas.supply.item.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import com.ozz.atlas.common.jpa.Status;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "상태 요청")
public class ChangeItemStatusRequest {

    @NotNull
    @Schema(description = "상태", example = "ACTIVE")
    private Status status;
}
