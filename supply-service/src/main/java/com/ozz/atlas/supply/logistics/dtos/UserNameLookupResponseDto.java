package com.ozz.atlas.supply.logistics.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserNameLookupResponseDto {

    private String userPublicId;

    private String userName;
}
