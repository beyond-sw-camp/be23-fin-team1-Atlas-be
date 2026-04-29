package com.ozz.atlas.auth.dtos.organization;

import com.ozz.atlas.auth.domain.Organization;
import com.ozz.atlas.auth.domain.OrganizationType;
import com.ozz.atlas.common.jpa.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "조직 상세 정보")
public class OrganizationDetailDto {

    @Schema(description = "조직 공개 식별자", example = "org_01HZX9X5D4P2Q7F8R9S1T2U3V4")
    private String organizationPublicId;

    @Schema(description = "조직 내부 ID", example = "1")
    private Long organizationId;

    @Schema(description = "조직 유형", example = "SUPPLIER")
    private OrganizationType organizationType;

    @Schema(description = "조직명", example = "아틀라스 푸드 서플라이어")
    private String organizationName;

    @Schema(description = "조직 영문명", example = "Atlas Foods Supplier")
    private String organizationEnglishName;

    @Schema(description = "조직 코드", example = "ATLAS")
    private String organizationAlias;

    @Schema(description = "사업자 등록번호", example = "123-45-67890", nullable = true)
    private String businessNo;

    @Schema(description = "담당자 이름", example = "Minji")
    private String contactFirstName;

    @Schema(description = "담당자 미들네임", example = "J", nullable = true)
    private String contactMiddleName;

    @Schema(description = "담당자 성", example = "Kim")
    private String contactLastName;

    @Schema(description = "담당자 이메일", example = "minji.kim@atlasfoods.com", nullable = true)
    private String contactEmail;

    @Schema(description = "담당자 연락처", example = "010-1234-5678")
    private String contactPhone;

    @Schema(description = "조직 상태", example = "ACTIVE")
    private Status status;

    private String organizationImageAttachmentPublicId;

    private String organizationImageThumbPath;

    private long memberCount;


    public static OrganizationDetailDto fromEntity(Organization organization, long memberCount) {
        return OrganizationDetailDto.builder()
                .organizationPublicId(organization.getPublicId())
                .organizationId(organization.getOrganizationId())
                .organizationType(organization.getOrganizationType())
                .organizationName(organization.getOrganizationName())
                .organizationEnglishName(organization.getOrganizationEnglishName())
                .organizationAlias(organization.getOrganizationAlias())
                .businessNo(organization.getBusinessNo())
                .contactFirstName(organization.getContactFirstName())
                .contactMiddleName(organization.getContactMiddleName())
                .contactLastName(organization.getContactLastName())
                .contactEmail(organization.getContactEmail())
                .contactPhone(organization.getContactPhone())
                .status(organization.getStatus())
                .organizationImageAttachmentPublicId(organization.getOrganizationImageAttachmentPublicId())
                .organizationImageThumbPath(organization.getOrganizationImageThumbPath())
                .memberCount(memberCount)
                .build();
    }
}
