package com.ozz.atlas.auth.search.document;

import com.ozz.atlas.auth.domain.Organization;
import com.ozz.atlas.auth.domain.OrganizationType;
import com.ozz.atlas.common.jpa.Status;
import org.springframework.data.annotation.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(indexName = "organizations")
public class OrganizationDocument {

    @Id
    private Long organizationId;

    private String publicId;

    private OrganizationType organizationType;

    private String organizationName;

    private String businessNo;

    private String contactFirstName;

    private String contactMiddleName;

    private String contactLastName;

    private String contactEmail;

    private String contactPhone;

    private Status status;

    public static OrganizationDocument fromEntity(Organization organization) {
        return OrganizationDocument.builder()
                .organizationId(organization.getOrganizationId())
                .publicId(organization.getPublicId())
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
