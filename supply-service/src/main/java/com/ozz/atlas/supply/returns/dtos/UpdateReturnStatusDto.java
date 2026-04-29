package com.ozz.atlas.supply.returns.dtos;

import com.ozz.atlas.supply.returns.domain.ReturnStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReturnStatusDto {
    @NotNull(message = "상태 값은 필수입니다.")
    private ReturnStatus returnStatus;

    @NotBlank(message = "상태 변경 사유는 필수입니다.")
    private String reason;

    private List<String> attachmentPublicIds;
}