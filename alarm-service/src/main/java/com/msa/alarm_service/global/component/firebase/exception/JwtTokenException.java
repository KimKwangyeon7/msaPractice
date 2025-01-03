package com.msa.alarm_service.global.component.firebase.exception;

import lombok.Getter;

@Getter
public class JwtTokenException extends RuntimeException {

    private final JwtTokenErrorCode errorCode;

    public JwtTokenException(JwtTokenErrorCode errorCode) {
        super(errorCode.getErrorMessage());
        this.errorCode = errorCode;
    }
}
