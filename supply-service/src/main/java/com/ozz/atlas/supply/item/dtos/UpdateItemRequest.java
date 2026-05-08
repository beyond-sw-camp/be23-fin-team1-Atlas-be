package com.ozz.atlas.supply.item.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import com.ozz.atlas.supply.item.domain.ItemUnit;
import com.ozz.atlas.supply.item.domain.SupplyType;
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
@Schema(description = "날짜 요청")
public class UpdateItemRequest {

    @NotBlank
    @Size(max = 26)
    @Schema(description = "공개 식별자", example = "sample_public_id")
    private String itemCategoryPublicId;

    @NotNull
    @Schema(description = "유형", example = "DEFAULT")
    private SupplyType supplyType;

    @NotBlank
    @Size(max = 100)
    @Schema(description = "이름", example = "샘플 이름")
    private String itemName;

    @NotNull
    @Schema(description = "unit 값", example = "sample")
    private ItemUnit unit;

    @NotBlank
    @Size(max = 100)
    @Schema(description = "spec 값", example = "sample")
    private String spec;

    @Schema(description = "가격", example = "1", nullable = true)
    private BigDecimal unitPrice;

    @NotNull
    @Min(0)
    @Schema(description = "shelf Life Days 값", example = "1")
    private Integer shelfLifeDays;

    @Schema(description = "공개 식별자", example = "sample_public_id", nullable = true)
    private String originLogisticsNodePublicId;

}
