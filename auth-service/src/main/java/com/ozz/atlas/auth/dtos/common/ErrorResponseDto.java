package com.ozz.atlas.auth.dtos.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Error 값 응답")
public class ErrorResponseDto {

    // HTTP 상태 코드
    @Schema(description = "상태", example = "ACTIVE", nullable = true)
    private int status;

    // 프론트와 로그에서 쓸 에러 코드
    @Schema(description = "코드", example = "CODE-001", nullable = true)
    private String code;

    // 화면에 보여줄 메시지
    @Schema(description = "메시지", example = "샘플 내용", nullable = true)
    private String message;
}
