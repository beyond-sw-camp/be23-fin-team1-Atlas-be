package com.ozz.atlas.supply.returns.dtos;

import com.ozz.atlas.supply.returns.domain.ReturnType;
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
public class CreateReturnRequestDto {
    @NotBlank(message = "업무 번호는 필수입니다.")
    private String returnNumber;

    private String sourceShipmentPublicId;

    @NotBlank(message = "요청 조직 식별자는 필수입니다.")
    private String requestOrganizationPublicId;

    @NotBlank(message = "대상 조직 식별자는 필수입니다.")
    private String targetOrganizationPublicId;

    @NotNull(message = "반품 유형은 필수입니다.")
    private ReturnType returnType;

    @NotBlank(message = "반품 사유는 필수입니다.")
    private String returnReason;

    private List<String> attachmentPublicIds;

    @NotEmpty(message = "반품 품목은 최소 1개 이상이어야 합니다.")
    @Valid
    private List<CreateReturnItemDto> items;
}