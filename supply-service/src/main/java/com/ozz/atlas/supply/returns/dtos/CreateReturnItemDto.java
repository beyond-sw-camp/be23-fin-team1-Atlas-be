package com.ozz.atlas.supply.returns.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateReturnItemDto {
    @NotBlank(message = "품목 ID는 필수입니다.")
    private String itemPublicId;

    private String lotPublicId;

    @Positive(message = "반품 수량은 0보다 커야 합니다.")
    private BigDecimal returnQty;

    @NotBlank(message = "단위는 필수입니다.")
    private String unit;

    private String detailReason;
    
    private List<String> attachmentPublicIds;
}