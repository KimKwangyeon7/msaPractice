package com.msa.chat_service.global.component.kafka.dto.response;

import com.msa.chat_service.global.component.kafka.dto.info.WordCountInfo;

import java.util.List;

public record WordCountResponse(
        List<WordCountInfo> wordCounts
) {
}
