package com.ozz.atlas.supply.lot.dtos;

import com.ozz.atlas.supply.lot.domain.QualityStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateQualityStatusRequestDto {
    @NotNull(message = "품질 상태는 필수입니다.")
    private QualityStatus qualityStatus;
}