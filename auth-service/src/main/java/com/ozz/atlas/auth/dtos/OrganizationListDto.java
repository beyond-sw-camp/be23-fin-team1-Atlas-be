package com.ozz.atlas.auth.dtos;

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
@Schema(description = "조직 목록")
public class OrganizationListDto {
    @Schema(description = "조직 공개 식별자", example = "org_01HZX9X5D4P2Q7F8R9S1T2U3V4")
    private String organizationPublicId;
    @Schema(description = "조직 유형", example = "SUPPLIER")
    private OrganizationType organizationType;
    @Schema(description = "조직명", example = "아틀라스 푸드 서플라이어")
    private String organizationName;
    @Schema(description = "조직 영문명", example = "Atlas Foods Supplier")
    private String organizationEnglishName;
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
    @Schema(description = "공급망 단계", example = "1", nullable = true)
    private Integer tierLevel;


    public static OrganizationListDto fromEntity(Organization organization){
        return OrganizationListDto.builder()
                .organizationPublicId(organization.getPublicId())
                .organizationType(organization.getOrganizationType())
                .organizationName(organization.getOrganizationName())
                .organizationEnglishName(organization.getOrganizationEnglishName())
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
