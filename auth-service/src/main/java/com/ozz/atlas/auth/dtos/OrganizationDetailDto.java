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
public class OrganizationDetailDto {
    private String organizationPublicId;
    private Long organizationId;
    private OrganizationType organizationType;
    private String organizationName;
    private String businessNo;
    private String contactFirstName;
    private String contactMiddleName;
    private String contactLastName;
    private String contactEmail;
    private String contactPhone;
    private Status status;

    public static OrganizationDetailDto fromEntity(Organization organization) {
        return OrganizationDetailDto.builder()
                .organizationPublicId(organization.getPublicId())
                .organizationId(organization.getOrganizationId())
                .organizationType(organization.getOrganizationType())
                .organizationName(organization.getOrganizationName())
                .businessNo(organization.getBusinessNo())
                .contactFirstName(organization.getContactFirstName())
                .contactMiddleName(organization.getContactMiddleName())
                .contactLastName(organization.getContactLastName())
                .contactEmail(organization.getContactEmail())
                .contactPhone(organization.getContactPhone())
                .status(organization.getStatus())
                .build();
    }
}
