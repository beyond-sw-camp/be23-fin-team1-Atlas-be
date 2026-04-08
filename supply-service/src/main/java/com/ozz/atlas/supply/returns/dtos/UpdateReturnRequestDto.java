package com.ozz.atlas.supply.returns.dtos;

import com.ozz.atlas.supply.returns.domain.ReturnType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReturnRequestDto {
    private ReturnType returnType;
    private String returnReason;
    private List<String> attachmentPublicIds;
}