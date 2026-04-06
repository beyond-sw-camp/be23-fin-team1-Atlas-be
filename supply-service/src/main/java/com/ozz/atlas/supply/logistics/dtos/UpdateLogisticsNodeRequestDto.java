package com.ozz.atlas.supply.logistics.dtos;

import com.ozz.atlas.supply.logistics.domain.LogisticsNodeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UpdateLogisticsNodeRequestDto {

    @NotBlank
    private String nodeCode;

    @NotBlank
    private String nodeName;

    @NotNull
    private LogisticsNodeType nodeType;

    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
}
