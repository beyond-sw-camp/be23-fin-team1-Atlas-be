package com.ozz.atlas.supply.lot.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateLotRequestDto {
    @NotBlank(message = "로트 번호는 필수입니다.")
    private String lotNumber;

    @NotBlank(message = "원본 발주 상세 ID는 필수입니다.")
    private String sourcePoItemPublicId;

    @NotBlank(message = "협력사 ID는 필수입니다.")
    private String supplierPublicId;

    @NotBlank(message = "품목 ID는 필수입니다.")
    private String itemPublicId;

    private LocalDateTime manufacturedAt;
    private LocalDateTime expiredAt;

    @NotNull(message = "수량은 필수입니다.")
    @Positive(message = "수량은 0보다 커야 합니다.")
    private BigDecimal qty;

    @NotBlank(message = "단위는 필수입니다.")
    private String unit;

    private String currentNodePublicId;
}