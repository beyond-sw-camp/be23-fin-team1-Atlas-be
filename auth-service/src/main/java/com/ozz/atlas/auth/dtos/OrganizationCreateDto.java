package com.ozz.atlas.auth.dtos;

import com.ozz.atlas.auth.domain.Organization;
import com.ozz.atlas.auth.domain.OrganizationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrganizationCreateDto {
    @NotNull(message = "조직 유형은 비어있으면 안 됩니다.")
    private OrganizationType organizationType;

    @NotBlank(message = "조직명은 비어있으면 안 됩니다.")
    private String organizationName;

    private String businessNo;

    @NotBlank(message = "담당자 이름은 비어있으면 안 됩니다.")
    private String contactFirstName;
    private String contactMiddleName;
    @NotBlank(message = "담당자 성은 비어있으면 안 됩니다.")
    private String contactLastName;

    @NotBlank(message = "이메일은 비어있으면 안 됩니다.")
    private String contactEmail;

    @NotBlank(message = "연락처는 비어있으면 안 됩니다.")
    private String contactPhone;

    private Integer tierLevel;

    public Organization toEntity() {
        return Organization.builder()
                .organizationType(this.organizationType)
                .organizationName(this.organizationName)
                .businessNo(this.businessNo)
                .contactFirstName(this.contactFirstName)
                .contactMiddleName(this.contactMiddleName)
                .contactLastName(this.contactLastName)
                .contactEmail(this.contactEmail)
                .contactPhone(this.contactPhone)
                .tierLevel(this.tierLevel)
                .build();
    }
}
