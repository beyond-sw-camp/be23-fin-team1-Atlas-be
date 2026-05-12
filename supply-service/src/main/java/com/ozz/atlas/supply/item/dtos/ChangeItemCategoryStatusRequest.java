package com.ozz.atlas.supply.item.dtos;

import com.ozz.atlas.common.jpa.Status;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChangeItemCategoryStatusRequest {

    @NotNull
    private Status status;
}
