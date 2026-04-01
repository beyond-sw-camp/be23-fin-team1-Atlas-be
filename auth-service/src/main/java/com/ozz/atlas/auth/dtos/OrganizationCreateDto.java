package com.ozz.atlas.auth.dtos;

import com.ozz.atlas.auth.domain.Organization;
import com.ozz.atlas.auth.domain.OrganizationType;
import com.ozz.atlas.auth.domain.Status;
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

    @NotBlank(message = "담당자명은 비어있으면 안 됩니다.")
    private String contactName;

    private String contactEmail;

    @NotBlank(message = "연락처는 비어있으면 안 됩니다.")
    private String contactPhone;

    public Organization toEntity() {
        return Organization.builder()
                .organizationType(this.organizationType)
                .organizationName(this.organizationName)
                .businessNo(this.businessNo)
                .contactName(this.contactName)
                .contactEmail(this.contactEmail)
                .contactPhone(this.contactPhone)
                .status(Status.ACTIVE)
                .build();
    }
}
