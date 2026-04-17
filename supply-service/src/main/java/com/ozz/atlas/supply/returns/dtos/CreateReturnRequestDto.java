package com.ozz.atlas.supply.returns.dtos;

import com.ozz.atlas.supply.returns.domain.ReturnType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "반품 생성 요청")
public class CreateReturnRequestDto {
    @NotBlank(message = "업무 번호는 필수입니다.")
    @Schema(description = "반품 번호", example = "RET-2026-0007")
    private String returnNumber;

    @Schema(description = "원본 출하 공개 식별자", example = "ship_01HZY1SHIPMENT123456789", nullable = true)
    private String sourceShipmentPublicId;

    @NotBlank(message = "요청 조직 식별자는 필수입니다.")
    @Schema(description = "반품 요청 조직 공개 식별자", example = "org_req_01HZY2ORGREQ123456")
    private String requestOrganizationPublicId;

    @NotBlank(message = "대상 조직 식별자는 필수입니다.")
    @Schema(description = "반품 대상 조직 공개 식별자", example = "org_tgt_01HZY2ORGTGT123456")
    private String targetOrganizationPublicId;

    @NotNull(message = "반품 유형은 필수입니다.")
    @Schema(description = "반품 유형", example = "QUALITY_ISSUE")
    private ReturnType returnType;

    @NotBlank(message = "반품 사유는 필수입니다.")
    @Schema(description = "반품 사유", example = "유통기한 임박 품목 회수")
    private String returnReason;

    @Schema(description = "반품 요청 자체에 연결된 첨부 파일 공개 식별자 목록", example = "[\"att_01HZY2ATT10\"]")
    private List<String> attachmentPublicIds;

    @NotEmpty(message = "반품 품목은 최소 1개 이상이어야 합니다.")
    @Valid
    @Schema(description = "반품 품목 목록")
    private List<CreateReturnItemDto> items;
}
