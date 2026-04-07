package com.ozz.atlas.supply.productionline.dtos;

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
public class ProductionLineStatusUpdateDto {

    @NotNull
    private Status status;
}
