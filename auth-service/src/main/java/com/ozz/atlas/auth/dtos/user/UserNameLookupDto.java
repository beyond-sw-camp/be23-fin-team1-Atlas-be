package com.ozz.atlas.auth.dtos.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "이름 모델")
public class UserNameLookupDto {

    @Schema(description = "사용자 공개 식별자", example = "sample_public_id", nullable = true)
    private String userPublicId;

    @Schema(description = "이름", example = "샘플 이름", nullable = true)
    private String userName;

}
