package com.ozz.atlas.auth.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrganizationUpdateDto {
    private String organizationName;

    private String businessNo;

    private String contactFirstName;

    private String contactMiddleName;

    private String contactLastName;

    private String contactEmail;

    private String contactPhone;

    private Integer tierLevel;

}
