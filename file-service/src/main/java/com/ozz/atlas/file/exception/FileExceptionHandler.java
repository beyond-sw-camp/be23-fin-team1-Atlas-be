package com.ozz.atlas.file.exception;

import com.ozz.atlas.common.exception.ErrorResponse;
import com.ozz.atlas.common.web.exception.BaseExceptionAdviceSupport;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class FileExceptionHandler extends BaseExceptionAdviceSupport {

    @ExceptionHandler(FileException.class)
    public ResponseEntity<ErrorResponse> handleFileException(FileException e) {
        return toErrorResponse(e);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        FileErrorCode errorCode = FileErrorCode.FILE_SIZE_EXCEEDED;
        return toErrorResponse(errorCode);
    }
}
