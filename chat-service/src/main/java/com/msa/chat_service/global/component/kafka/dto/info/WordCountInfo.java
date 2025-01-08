package com.msa.chat_service.global.component.kafka.dto.info;

public record WordCountInfo(
        String word,
        Long count
) {
}
