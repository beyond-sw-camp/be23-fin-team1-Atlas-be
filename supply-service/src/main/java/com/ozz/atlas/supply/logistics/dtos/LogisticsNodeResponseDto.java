package com.ozz.atlas.supply.logistics.dtos;

import com.ozz.atlas.supply.logistics.domain.LogisticsNodeCapacityStatus;
import com.ozz.atlas.supply.logistics.domain.LogisticsNode;
import com.ozz.atlas.supply.logistics.domain.LogisticsNodeType;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "창고 응답")
public class LogisticsNodeResponseDto {

    @Schema(description = "창고 공개 식별자", example = "01HZY1NODE12345678901234")
    private String publicId;
    @Schema(description = "창고 소유 조직 공개 식별자", example = "01HQ456789ABCDEF01HQ456789")
    private String organizationPublicId;
    @Schema(description = "자동 생성 창고 코드", example = "WH-CHO1-001")
    private String nodeCode;
    @Schema(description = "창고명", example = "서울 물류창고")
    private String nodeName;
    @Schema(description = "거점 유형", example = "WAREHOUSE")
    private LogisticsNodeType nodeType;
    @Schema(description = "창고 주소", example = "서울특별시 강남구 테헤란로 152")
    private String address;
    @Schema(description = "위도", example = "37.5000242")
    private BigDecimal latitude;
    @Schema(description = "경도", example = "127.0365086")
    private BigDecimal longitude;
    @Schema(description = "창고 상태", example = "AVAILABLE")
    private LogisticsNodeCapacityStatus capacityStatus;
    @Schema(description = "활성 여부", example = "true")
    private boolean active;
    @Schema(description = "생성 시각", example = "2026-04-24T09:40:39")
    private LocalDateTime createdAt;
    @Schema(description = "수정 시각", example = "2026-04-24T09:40:39")
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
