package com.ozz.atlas.auth.dtos;

import com.ozz.atlas.auth.domain.Organization;
import com.ozz.atlas.auth.domain.OrganizationType;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "조직 생성 요청")
public class OrganizationCreateDto {

    @NotNull(message = "조직 유형은 비어있으면 안 됩니다.")
    @Schema(description = "조직 유형", example = "SUPPLIER")
    private OrganizationType organizationType;

    @NotBlank(message = "조직명은 비어있으면 안 됩니다.")
    @Schema(description = "조직명", example = "Atlas Foods Supplier")
    private String organizationName;

    @NotBlank(message = "조직 영문명은 비어있으면 안 됩니다.")
    @Schema(description = "조직 영문명", example = "Atlas Foods Supplier")
    private String organizationEnglishName;

    @NotBlank(message = "조직 코드는 비어있으면 안 됩니다.")
    @Schema(description = "조직 alias", example = "ATLAS")
    private String organizationAlias;

    @Schema(description = "사업자 등록번호", example = "123-45-67890")
    private String businessNo;

    @NotBlank(message = "담당자 이름은 비어있으면 안 됩니다.")
    @Schema(description = "담당자 이름", example = "Minji")
    private String contactFirstName;

    @Schema(description = "담당자 미들네임", example = "J")
    private String contactMiddleName;

    @NotBlank(message = "담당자 성은 비어있으면 안 됩니다.")
    @Schema(description = "담당자 성", example = "Kim")
    private String contactLastName;

    @NotBlank(message = "이메일은 비어있으면 안 됩니다.")
    @Schema(description = "담당자 이메일", example = "minji.kim@atlasfoods.com")
    private String contactEmail;

    @NotBlank(message = "연락처는 비어있으면 안 됩니다.")
    @Schema(description = "담당자 연락처", example = "010-1234-5678")
    private String contactPhone;

    public Organization toEntity() {
        return Organization.builder()
                .organizationType(this.organizationType)
                .organizationName(this.organizationName)
                .organizationEnglishName(this.organizationEnglishName)
                .organizationAlias(this.organizationAlias.trim().toUpperCase())
                .businessNo(this.businessNo)
                .contactFirstName(this.contactFirstName)
                .contactMiddleName(this.contactMiddleName)
                .contactLastName(this.contactLastName)
                .contactEmail(this.contactEmail)
                .contactPhone(this.contactPhone)
                .build();
    }
}
