package com.ozz.atlas.supply.item.dtos;

import com.ozz.atlas.common.jpa.Status;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeItemStatusRequest {

    @NotNull
    private Status status;
}
