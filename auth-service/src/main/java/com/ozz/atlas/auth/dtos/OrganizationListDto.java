package com.ozz.atlas.auth.dtos;

import com.ozz.atlas.auth.domain.Organization;
import com.ozz.atlas.auth.domain.OrganizationType;
import com.ozz.atlas.common.jpa.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrganizationListDto {
    private String organizationPublicId;
    private OrganizationType organizationType;
    private String organizationName;
    private String contactFirstName;
    private String contactMiddleName;
    private String contactLastName;
    private String contactEmail;
    private String contactPhone;
    private Status status;
    private Integer tierLevel;


    public static OrganizationListDto fromEntity(Organization organization){
        return OrganizationListDto.builder()
                .organizationPublicId(organization.getPublicId())
                .organizationType(organization.getOrganizationType())
                .organizationName(organization.getOrganizationName())
                .contactFirstName(organization.getContactFirstName())
                .contactMiddleName(organization.getContactMiddleName())
                .contactLastName(organization.getContactLastName())
                .contactEmail(organization.getContactEmail())
                .contactPhone(organization.getContactPhone())
                .status(organization.getStatus())
                .tierLevel(organization.getTierLevel())
                .build();

    }
}
