package com.msa.alarm_service.global.component.firebase.exception;


import lombok.Getter;

@Getter
public class FcmException extends RuntimeException {
    private final FcmErrorCode errorCode;

    public FcmException(FcmErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}