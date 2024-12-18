package com.msa.community_service.domain.community.exception;

import lombok.Getter;

@Getter
public class CommunityException extends RuntimeException {
    private final CommunityErrorCode errorCode;

    public CommunityException(CommunityErrorCode errorCode) {
        super(errorCode.getErrorMessage());
        this.errorCode = errorCode;
    }
}
