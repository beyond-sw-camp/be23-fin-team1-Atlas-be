package com.ozz.atlas.supply.settlement.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserNameLookupResponseDto {

    private String userPublicId;

    private String userName;

}
