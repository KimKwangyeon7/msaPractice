package com.msa.chat_service.domain.chat.exception;

import com.msa.chat_service.global.common.dto.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ChatExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> invalidInputExceptionHandler(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();

        // 오류 메시지를 순서대로 추가합니다.
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(fieldName + "Error", message);
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Message.fail("validError", errors));
    }

    @ExceptionHandler(ChatException.class)
    public ResponseEntity<Message<Void>> memberException(ChatException e) {
        log.error("채팅 관련 오류: {}", e.getMessage());
        return ResponseEntity.status(e.getErrorCode().getHttpStatus()).body(Message.fail(null, e.getErrorCode().getErrorMessage()));
    }
}
