package com.ozz.atlas.file.exception;

import com.ozz.atlas.common.exception.BaseException;

public class FileException extends BaseException {
    public FileException(FileErrorCode errorCode) {
        super(errorCode);
    }
}
