package com.msa.chat_service.global.exception;

import lombok.Getter;

@Getter
public class GlobalException extends RuntimeException {
    private final GlobalErrorCode errorCode;

    public GlobalException(GlobalErrorCode errorCode) {
        super(errorCode.getErrorMessage());
        this.errorCode = errorCode;
    }
}
