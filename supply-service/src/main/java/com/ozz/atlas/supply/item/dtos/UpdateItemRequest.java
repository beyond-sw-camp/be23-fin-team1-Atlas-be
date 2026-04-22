package com.ozz.atlas.supply.item.dtos;

import com.ozz.atlas.supply.item.domain.ItemUnit;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateItemRequest {

    @NotBlank
    @Size(max = 26)
    private String itemCategoryPublicId;

    @NotBlank
    @Size(max = 100)
    private String itemName;

    @NotNull
    private ItemUnit unit;

    @NotBlank
    @Size(max = 100)
    private String spec;

    private BigDecimal unitPrice;

    @NotNull
    @Min(0)
    private Integer shelfLifeDays;
}
