package com.ozz.atlas.supply.item.dtos;

import com.ozz.atlas.supply.item.domain.ItemUnit;
import com.ozz.atlas.supply.item.domain.SupplyType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateItemRequest {

    @NotBlank
    @Size(max = 26)
    private String itemCategoryPublicId;

    @NotNull
    private SupplyType supplyType;

    @NotBlank
    @Size(max = 100)
    private String itemName;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal unitPrice;

    @NotNull
    private ItemUnit unit;

    @NotBlank
    @Size(max = 100)
    private String spec;

    @NotNull
    @Min(0)
    private Integer shelfLifeDays;

    private String originLogisticsNodePublicId;

}
