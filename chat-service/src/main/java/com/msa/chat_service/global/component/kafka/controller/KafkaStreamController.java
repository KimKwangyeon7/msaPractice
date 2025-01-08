package com.msa.chat_service.global.component.kafka.controller;

import com.msa.chat_service.global.common.dto.Message;
import com.msa.chat_service.global.component.kafka.dto.info.WordCountInfo;
import com.msa.chat_service.global.component.kafka.dto.response.RankingResponse;
import com.msa.chat_service.global.component.kafka.dto.response.WordCountResponse;
import com.msa.chat_service.global.component.kafka.service.KafkaStreamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Controller
@RequiredArgsConstructor
@RequestMapping("/chat")
public class KafkaStreamController {
    private final KafkaStreamService kafkaStreamService;

    @GetMapping("/rankings")
//    public ResponseEntity<Message<WordCountResponse>> getWordCount() {
//        WordCountResponse rankings = kafkaStreamService.getWordCounts();
//        return ResponseEntity.ok().body(Message.success(rankings));
//    }
    public String getWordCounts(Model model) {
        // 서비스에서 데이터 조회
        WordCountResponse wordCountResponse = kafkaStreamService.getWordCounts();
        for (WordCountInfo dto: wordCountResponse.wordCounts()){
            System.out.println(dto.word() + " " + dto.count());
        }
        model.addAttribute("wordCounts", wordCountResponse.wordCounts());
        return "wordCount"; // JSP 파일 이름
    }
}
