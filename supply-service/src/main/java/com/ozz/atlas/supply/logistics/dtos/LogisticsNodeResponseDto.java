package com.ozz.atlas.supply.logistics.dtos;

import com.ozz.atlas.supply.logistics.domain.LogisticsNodeCapacityStatus;
import com.ozz.atlas.supply.logistics.domain.LogisticsNode;
import com.ozz.atlas.supply.logistics.domain.LogisticsNodeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class LogisticsNodeResponseDto {

    private String publicId;
    private String organizationPublicId;
    private String nodeCode;
    private String nodeName;
    private LogisticsNodeType nodeType;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private LogisticsNodeCapacityStatus capacityStatus;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static LogisticsNodeResponseDto from(LogisticsNode node){
        return LogisticsNodeResponseDto.builder()
                .publicId(node.getPublicId())
                .organizationPublicId(node.getOrganizationPublicId())
                .nodeCode(node.getNodeCode())
                .nodeName(node.getNodeName())
                .nodeType(node.getNodeType())
                .address(node.getAddress())
                .latitude(node.getLatitude())
                .longitude(node.getLongitude())
                .capacityStatus(node.getCapacityStatus())
                .active(node.isActive())
                .createdAt(node.getCreatedAt())
                .updatedAt(node.getUpdatedAt())
                .build();
    }
}
