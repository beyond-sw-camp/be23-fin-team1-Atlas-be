package com.ozz.atlas.auth.dtos.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserNameLookupDto {

    private String userPublicId;

    private String userName;

}
