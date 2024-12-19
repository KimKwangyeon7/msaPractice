package com.msa.chat_service.global.component.kafka.controller;

import com.msa.chat_service.global.common.dto.Message;
import com.msa.chat_service.global.component.kafka.dto.response.RankingResponse;
import com.msa.chat_service.global.component.kafka.service.KafkaStreamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/kafka")
public class KafkaStreamController {
    private final KafkaStreamService kafkaStreamService;

    @GetMapping("/rankings")
    public ResponseEntity<Message<RankingResponse>> getWordCount() {
        RankingResponse rankings = kafkaStreamService.getRankings();
        return ResponseEntity.ok().body(Message.success(rankings));
    }
}
