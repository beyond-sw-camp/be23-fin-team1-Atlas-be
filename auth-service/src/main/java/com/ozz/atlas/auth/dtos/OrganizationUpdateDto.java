package com.ozz.atlas.auth.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "조직 수정 요청")
public class OrganizationUpdateDto {

    @Schema(description = "조직명", example = "아틀라스 푸드 서플라이어", nullable = true)
    private String organizationName;

    @Schema(description = "조직 영문명", example = "Atlas Foods Supplier", nullable = true)
    private String organizationEnglishName;

    @Schema(description = "조직 코드", example = "ATLAS", nullable = true)
    private String organizationAlias;

    @Schema(description = "사업자 등록번호", example = "123-45-67890", nullable = true)
    private String businessNo;

    @Schema(description = "담당자 이름", example = "Minji", nullable = true)
    private String contactFirstName;

    @Schema(description = "담당자 미들네임", example = "J", nullable = true)
    private String contactMiddleName;

    @Schema(description = "담당자 성", example = "Kim", nullable = true)
    private String contactLastName;

    @Schema(description = "담당자 이메일", example = "minji.kim@atlasfoods.com", nullable = true)
    private String contactEmail;

    @Schema(description = "담당자 연락처", example = "010-1234-5678", nullable = true)
    private String contactPhone;

}
