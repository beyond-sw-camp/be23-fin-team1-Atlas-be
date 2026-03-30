package com.ozz.atlas.common.exception;

public interface ErrorCode {
    int getStatus();
    String getCode();
    String getMessage();
}
