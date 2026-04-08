package com.ozz.atlas.supply.lot.dtos;

import com.ozz.atlas.supply.lot.domain.LotStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLotStatusRequestDto {
    @NotNull(message = "로트 상태는 필수입니다.")
    private LotStatus lotStatus;
}