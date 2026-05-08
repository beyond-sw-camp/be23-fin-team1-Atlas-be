package com.ozz.atlas.file.exception;

import com.ozz.atlas.common.exception.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import static org.assertj.core.api.Assertions.assertThat;

class FileExceptionHandlerTest {

    private final FileExceptionHandler handler = new FileExceptionHandler();

    @Test
    void fileException은_errorCode_기준_응답으로_변환한다() {
        ResponseEntity<ErrorResponse> response =
                handler.handleFileException(new FileException(FileErrorCode.FILE_NOT_FOUND));

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().code()).isEqualTo("FILE_003");
        assertThat(response.getBody().message()).isEqualTo("파일을 찾을 수 없습니다.");
    }

    @Test
    void maxUploadSizeExceededException은_파일_크기_초과_응답으로_변환한다() {
        ResponseEntity<ErrorResponse> response =
                handler.handleMaxUploadSizeExceededException(new MaxUploadSizeExceededException(10));

        assertThat(response.getStatusCode().value()).isEqualTo(413);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(413);
        assertThat(response.getBody().code()).isEqualTo("FILE_006");
        assertThat(response.getBody().message()).isEqualTo("업로드 가능한 파일 크기를 초과했습니다.");
    }
}
