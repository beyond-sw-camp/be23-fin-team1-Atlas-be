package com.ozz.atlas.auth.common.exception;

import com.ozz.atlas.common.exception.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void illegalArgumentException은_공통_BAD_REQUEST_응답으로_변환한다() {
        ResponseEntity<ErrorResponse> response =
                handler.handleIllegalArgument(new IllegalArgumentException("잘못된 요청입니다."));

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().code()).isEqualTo("BAD_REQUEST");
        assertThat(response.getBody().message()).isEqualTo("잘못된 요청입니다.");
    }
}
