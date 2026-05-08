package com.ozz.atlas.supply.inventory.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@Schema(description = "Create Item Inventory 값 요청")
public class CreateItemInventoryRequest {

    @NotBlank
    @Schema(description = "품목 공개 식별자", example = "sample_public_id")
    private String itemPublicId;

    @NotNull
    @Schema(description = "날짜", example = "2026-05-08")
    private LocalDate manufacturedDate;

    @NotNull
    @Positive
    @Schema(description = "수량", example = "1")
    private Long qty;

    @Schema(description = "메모", example = "샘플 내용", nullable = true)
    private String memo;

    @NotBlank
    @Schema(description = "물류 노드 공개 식별자", example = "sample_public_id")
    private String logisticsNodePublicId;
}
