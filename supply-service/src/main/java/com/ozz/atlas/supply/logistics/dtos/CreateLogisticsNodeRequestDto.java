package com.ozz.atlas.supply.logistics.dtos;

import com.ozz.atlas.supply.logistics.domain.LogisticsNode;
import com.ozz.atlas.supply.logistics.domain.LogisticsNodeType;
import com.ozz.atlas.supply.logistics.domain.LogisticsNodeCapacityStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Schema(description = "창고 생성 요청")
public class CreateLogisticsNodeRequestDto {

    @Schema(description = "창고명", example = "서울 물류창고")
    @NotBlank
    private String nodeName;

    @Schema(description = "거점 유형. 현재는 WAREHOUSE만 사용합니다.", example = "WAREHOUSE")
    private LogisticsNodeType nodeType;

    @Schema(description = "창고 주소. 저장 시 주소 기반 좌표가 자동 계산됩니다.", example = "서울특별시 강남구 테헤란로 152")
    @NotBlank(message = "창고 기본 주소는 비어있으면 안 됩니다.")
    private String baseAddress;

    @Schema(description = "동/층/호수 등 상세 주소", example = "12층 A호")
    private String detailAddress;

    @Schema(description = "창고 상태", example = "AVAILABLE")
    private LogisticsNodeCapacityStatus capacityStatus;

    // nodeCode와 좌표는 서비스에서 계산한 값을 받아 엔티티에 넣는다.
    public LogisticsNode toEntity(
            String organizationPublicId,
            String nodeCode,
            java.math.BigDecimal latitude,
            java.math.BigDecimal longitude
    ){
        String displayAddress = buildDisplayAddress(this.baseAddress, this.detailAddress);

        return LogisticsNode.builder()
                .organizationPublicId(organizationPublicId)
                .nodeCode(nodeCode)
                .nodeName(this.nodeName)
                .nodeType(LogisticsNodeType.WAREHOUSE)
                .baseAddress(this.baseAddress)
                .detailAddress(this.detailAddress)
                .address(displayAddress)
                .latitude(latitude)
                .longitude(longitude)
                .capacityStatus(this.capacityStatus == null ? LogisticsNodeCapacityStatus.EMPTY : this.capacityStatus)
                .active(true)
                .build();
    }
    private String buildDisplayAddress(String baseAddress, String detailAddress) {
        if (detailAddress == null || detailAddress.isBlank()) {
            return baseAddress;
        }
        return baseAddress + " " + detailAddress.trim();
    }
}
