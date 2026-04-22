package com.ozz.atlas.supply.logistics.dtos;

import com.ozz.atlas.supply.logistics.domain.LogisticsNode;
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
public class CreateLogisticsNodeRequestDto {

    @NotBlank
    private String nodeName;

    @NotNull
    private LogisticsNodeType nodeType;

    private String address;

    // nodeCode와 좌표는 서비스에서 계산한 값을 받아 엔티티에 넣는다.
    public LogisticsNode toEntity(
            String organizationPublicId,
            String nodeCode,
            java.math.BigDecimal latitude,
            java.math.BigDecimal longitude
    ){
        return LogisticsNode.builder()
                .organizationPublicId(organizationPublicId)
                .nodeCode(nodeCode)
                .nodeName(this.nodeName)
                .nodeType(this.nodeType)
                .address(this.address)
                .latitude(latitude)
                .longitude(longitude)
                .active(true)
                .build();
    }
}
