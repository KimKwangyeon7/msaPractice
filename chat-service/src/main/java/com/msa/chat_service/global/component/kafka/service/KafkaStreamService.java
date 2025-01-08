package com.msa.chat_service.global.component.kafka.service;


import com.msa.chat_service.global.component.kafka.dto.response.RankingResponse;
import com.msa.chat_service.global.component.kafka.dto.response.WordCountResponse;

public interface KafkaStreamService {
    RankingResponse getRankings();

    WordCountResponse getWordCounts();
}
