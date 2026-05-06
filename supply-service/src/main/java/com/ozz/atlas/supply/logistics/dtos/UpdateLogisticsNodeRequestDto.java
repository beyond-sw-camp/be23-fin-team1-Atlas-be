package com.ozz.atlas.supply.logistics.dtos;

import com.ozz.atlas.supply.logistics.domain.LogisticsNodeCapacityStatus;
import com.ozz.atlas.supply.logistics.domain.LogisticsNodeType;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "창고 수정 요청")
public class UpdateLogisticsNodeRequestDto {

    @Schema(description = "창고명", example = "서울 물류창고")
    @NotBlank
    private String nodeName;

    @Schema(description = "거점 유형. 현재는 WAREHOUSE만 사용합니다.", example = "WAREHOUSE")
    private LogisticsNodeType nodeType;

    @Schema(description = "창고 주소. 주소 변경 시 좌표가 다시 계산됩니다.", example = "서울특별시 강남구 테헤란로 152")
    private String address;

    @Schema(description = "창고 상태", example = "FULL")
    @NotNull(message = "창고 상태는 비어있으면 안 됩니다.")
    private LogisticsNodeCapacityStatus capacityStatus;

}
