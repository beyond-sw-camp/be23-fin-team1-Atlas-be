package com.ozz.atlas.auth.domain;

import com.ozz.atlas.auth.dtos.organization.OrganizationUpdateDto;
import com.ozz.atlas.common.jpa.BaseTimeEntity;
import com.ozz.atlas.common.id.PublicIdGenerator;
import com.ozz.atlas.common.jpa.Status;
import jakarta.persistence.*;
import lombok.*;

@Getter @ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Organization extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long organizationId;

    @Column(nullable = false, unique = true, length = 26, updatable = false)
    @Builder.Default
    private String publicId = PublicIdGenerator.next();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private OrganizationType organizationType;

    @Column(nullable = false, length = 100)
    private String organizationName;

    @Column(nullable = false, length = 100)
    private String organizationEnglishName;

    @Column(nullable = false, unique = true, length = 10)
    private String organizationAlias;

    @Column(length = 30)
    private String businessNo;

    @Column(nullable = false, length = 10)
    private String contactFirstName;

    private String contactMiddleName;

    @Column(nullable = false, length = 10)
    private String contactLastName;

    @Column(length = 100)
    private String contactEmail;

    @Column(nullable = false, length = 30)
    private String contactPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.ACTIVE;

    @Column(length = 26)
    private String organizationImageAttachmentPublicId;

    @Column(length = 500)
    private String organizationImageThumbPath;


    public void updateOrganization(OrganizationUpdateDto dto) {
        if (dto.getOrganizationName() != null && !dto.getOrganizationName().isBlank()) {
            this.organizationName = dto.getOrganizationName();
        }
        if (dto.getOrganizationEnglishName() != null && !dto.getOrganizationEnglishName().isBlank()) {
            this.organizationEnglishName = dto.getOrganizationEnglishName();
        }
        if (dto.getOrganizationAlias() != null && !dto.getOrganizationAlias().isBlank()) {
            this.organizationAlias = dto.getOrganizationAlias().trim().toUpperCase();
        }
        if (dto.getBusinessNo() != null && !dto.getBusinessNo().isBlank()) {
            this.businessNo = dto.getBusinessNo();
        }
        if (dto.getContactFirstName() != null && !dto.getContactFirstName().isBlank()) {
            this.contactFirstName = dto.getContactFirstName();
        }
        if (dto.getContactMiddleName() != null) {
            this.contactMiddleName = dto.getContactMiddleName();
        }
        if (dto.getContactLastName() != null && !dto.getContactLastName().isBlank()) {
            this.contactLastName = dto.getContactLastName();
        }
        if (dto.getContactEmail() != null && !dto.getContactEmail().isBlank()) {
            this.contactEmail = dto.getContactEmail();
        }
        if (dto.getContactPhone() != null && !dto.getContactPhone().isBlank()) {
            this.contactPhone = dto.getContactPhone();
        }

        if (dto.getOrganizationImageAttachmentPublicId() != null) {
            this.organizationImageAttachmentPublicId = dto.getOrganizationImageAttachmentPublicId();
        }

        if (dto.getOrganizationImageThumbPath() != null) {
            this.organizationImageThumbPath = dto.getOrganizationImageThumbPath();
        }

    }

    public void changeStatus(Status status) {
        this.status = status;
    }
}
