package com.msa.chat_service.domain.chat.controller;

import com.msa.chat_service.domain.chat.dto.request.ChatMessageRequest;
import com.msa.chat_service.domain.chat.service.ChatMessageService;
import com.msa.chat_service.global.component.kafka.KafkaConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ChatMessageController {
    private final ChatMessageService chatMessageService;

    @MessageMapping("/message/{roomId}")
    public void sendMessage(@DestinationVariable Long roomId, @Payload ChatMessageRequest request) {
        chatMessageService.send(KafkaConstants.KAFKA_TOPIC, request);
    }
}
