package com.ozz.atlas.supply.inventory.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class UpdateItemInventoryRequest {

    @NotNull
    private LocalDate manufacturedDate;

    @NotNull
    @Positive
    private Long qty;

    private String memo;

    @NotBlank
    private String logisticsNodePublicId;
}
