package com.ozz.atlas.supply.item.dtos;

import com.ozz.atlas.supply.item.domain.ItemSpec;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateItemRequest {
    @NotNull
    private Long itemCategoryId;

    @NotBlank
    @Size(max = 50)
    private String itemCode;

    @NotBlank
    @Size(max = 100)
    private String itemName;

    @NotBlank
    @Size(max = 20)
    private String unit;

    @NotNull
    private ItemSpec spec;

    @NotNull
    @Min(0)
    private Integer shelfLifeDays;

    @NotNull
    @Min(0)
    @Max(1)
    private Integer activeYn;

}
