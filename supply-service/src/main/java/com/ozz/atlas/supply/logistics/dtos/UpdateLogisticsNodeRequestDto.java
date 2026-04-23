package com.ozz.atlas.supply.logistics.dtos;

import com.ozz.atlas.supply.logistics.domain.LogisticsNodeCapacityStatus;
import com.ozz.atlas.supply.logistics.domain.LogisticsNodeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UpdateLogisticsNodeRequestDto {

    @NotBlank
    private String nodeName;

    @NotNull
    private LogisticsNodeType nodeType;

    private String address;
    @NotNull(message = "창고 상태는 비어있으면 안 됩니다.")
    private LogisticsNodeCapacityStatus capacityStatus;

}
