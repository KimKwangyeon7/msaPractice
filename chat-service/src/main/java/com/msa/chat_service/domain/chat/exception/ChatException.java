package com.msa.chat_service.domain.chat.exception;

import lombok.Getter;

@Getter
public class ChatException extends RuntimeException {
    private final ChatErrorCode errorCode;

    public ChatException(ChatErrorCode errorCode) {
        super(errorCode.getErrorMessage());
        this.errorCode = errorCode;
    }
}
