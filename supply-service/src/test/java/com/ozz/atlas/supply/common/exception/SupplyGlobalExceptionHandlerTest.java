package com.ozz.atlas.supply.common.exception;

import com.ozz.atlas.common.exception.BaseException;
import com.ozz.atlas.common.exception.ErrorCode;
import com.ozz.atlas.common.exception.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.assertj.core.api.Assertions.assertThat;

class SupplyGlobalExceptionHandlerTest {

    private final SupplyGlobalExceptionHandler handler = new SupplyGlobalExceptionHandler();

    @Test
    void baseException은_errorCode_기준_응답으로_변환한다() {
        ResponseEntity<ErrorResponse> response = handler.handleBaseException(
                new BaseException(new TestErrorCode(409, "TEST_409", "충돌입니다."))
        );

        assertThat(response.getStatusCode().value()).isEqualTo(409);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(409);
        assertThat(response.getBody().code()).isEqualTo("TEST_409");
        assertThat(response.getBody().message()).isEqualTo("충돌입니다.");
    }

    @Test
    void validationException은_공통_400_응답으로_변환한다() {
        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "orderedQty", "발주 수량은 필수입니다."));
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleValidationException(exception);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().code()).isEqualTo("COMMON_400");
        assertThat(response.getBody().message()).isEqualTo("발주 수량은 필수입니다.");
    }

    private record TestErrorCode(int status, String code, String message) implements ErrorCode {

        @Override
        public int getStatus() {
            return status;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getMessage() {
            return message;
        }
    }
}
