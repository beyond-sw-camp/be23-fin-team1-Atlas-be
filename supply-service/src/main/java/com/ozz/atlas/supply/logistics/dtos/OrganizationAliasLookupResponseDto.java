package com.ozz.atlas.supply.logistics.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "조직 alias 조회 응답")
public class OrganizationAliasLookupResponseDto {

    @Schema(description = "조직 공개 식별자", example = "01HQ456789ABCDEF01HQ456789")
    private String organizationPublicId;
    @Schema(description = "창고 코드 생성에 사용하는 조직 alias", example = "CHO1")
    private String organizationAlias;
}
