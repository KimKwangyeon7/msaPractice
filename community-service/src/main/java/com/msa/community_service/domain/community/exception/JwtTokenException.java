package com.msa.community_service.domain.community.exception;

import lombok.Getter;

@Getter
public class JwtTokenException extends RuntimeException {

    private final JwtTokenErrorCode errorCode;

    public JwtTokenException(JwtTokenErrorCode errorCode) {
        super(errorCode.getErrorMessage());
        this.errorCode = errorCode;
    }
}
