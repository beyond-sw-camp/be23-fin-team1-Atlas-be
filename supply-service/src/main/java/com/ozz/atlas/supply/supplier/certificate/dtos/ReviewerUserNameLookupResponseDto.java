package com.ozz.atlas.supply.supplier.certificate.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewerUserNameLookupResponseDto {

    private String userPublicId;
    private String userName;
}
