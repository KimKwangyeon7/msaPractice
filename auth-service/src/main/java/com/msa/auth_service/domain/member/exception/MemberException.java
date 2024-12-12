package com.msa.auth_service.domain.member.exception;

import lombok.Getter;

@Getter
public class MemberException extends RuntimeException {

    private final MemberErrorCode errorCode;

    public MemberException(MemberErrorCode errorCode) {
        super(errorCode.getErrorMessage());
        this.errorCode = errorCode;
    }
}
