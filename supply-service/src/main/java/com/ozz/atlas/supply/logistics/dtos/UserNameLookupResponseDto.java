package com.ozz.atlas.supply.logistics.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "이름 응답")
public class UserNameLookupResponseDto {

    @Schema(description = "사용자 공개 식별자", example = "sample_public_id", nullable = true)
    private String userPublicId;

    @Schema(description = "이름", example = "샘플 이름", nullable = true)
    private String userName;
}
