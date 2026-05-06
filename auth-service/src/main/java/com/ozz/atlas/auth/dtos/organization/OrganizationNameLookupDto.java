package com.ozz.atlas.auth.dtos.organization;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationNameLookupDto {

    private String organizationPublicId;

    private String organizationName;

    private String contactFirstName;

    private String contactMiddleName;

    private String contactLastName;

    private String contactPhone;

}
