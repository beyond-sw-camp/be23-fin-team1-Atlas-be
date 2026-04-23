package com.ozz.atlas.auth.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErrorResponseDto {

    // HTTP 상태 코드
    private int status;

    // 프론트와 로그에서 쓸 에러 코드
    private String code;

    // 화면에 보여줄 메시지
    private String message;
}
