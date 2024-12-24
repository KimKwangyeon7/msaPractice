package com.msa.chat_service.global.component.kafka.consumer;


import com.msa.chat_service.domain.chat.dto.response.ChatMessageResponse;
import com.msa.chat_service.domain.chat.entity.ChatMessage;
import com.msa.chat_service.global.component.kafka.KafkaConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaConsumer {
    private final SimpMessagingTemplate simpMessagingTemplate;
//    private final DataRepository dataRepository;

    @KafkaListener(topics = KafkaConstants.KAFKA_TOPIC, groupId = "chat-service-group")
    public void handleChatMessage(ChatMessageResponse chatMessage) {
        log.info("채팅 메시지 이벤트 수신 : {} {} {} ", chatMessage.getContent(), chatMessage.getChatRoomId(), chatMessage.getSenderId());
        simpMessagingTemplate.convertAndSend("/topic/public/rooms/" + chatMessage.getChatRoomId(), chatMessage);
    }

//    @KafkaListener(topics = "chat.room.message.sending", groupId = "chat-service-group")
//    public void consume(ChatMessageResponse message) {
//        log.info("Received message: {}", message);
//    }

//    @KafkaListener(topics = KafkaConstants.KAFKA_TOPIC_ANALYSIS)
//    public void handleCommercialAnalysis(CommercialAnalysisKafkaRequest message) {
//        log.info("상업 분석 메시지 수신 : {}", message);
//    }

//    @KafkaListener(topics = KafkaConstants.KAFKA_TOPIC_DATA)
//    public void handleUserData(DataInfo message) {
//        log.info("상업 분석 메시지 수신 : {}", message);
////        if (!message.commercialCode().equals("0")) {
////            DataDocument dataDocument = DataDocument.builder()
////                    .userId(message.userId())
////                    .commercialCode(Long.parseLong(message.commercialCode()))
////                    .action(message.action())
////                    .build();
////            dataRepository.save(dataDocument);
////        }
//    }

}