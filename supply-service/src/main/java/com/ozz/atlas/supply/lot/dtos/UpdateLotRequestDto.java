package com.ozz.atlas.supply.lot.dtos;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLotRequestDto {
    @Positive(message = "수량은 0보다 커야 합니다.")
    private BigDecimal qty;
    
    private LocalDateTime expiredAt;
    private Long currentNodeId;
}