package com.msa.chat_service.domain.chat.service;

import com.msa.chat_service.domain.chat.dto.request.ChatMessageRequest;
import com.msa.chat_service.domain.chat.dto.response.ChatMessageResponse;
import com.msa.chat_service.domain.chat.entity.ChatMessage;
import com.msa.chat_service.domain.chat.entity.ChatRoom;
import com.msa.chat_service.domain.chat.exception.ChatErrorCode;
import com.msa.chat_service.domain.chat.exception.ChatException;
import com.msa.chat_service.domain.chat.repository.ChatMessageRepository;
import com.msa.chat_service.domain.chat.repository.ChatRoomMemberRepository;
import com.msa.chat_service.domain.chat.repository.ChatRoomRepository;
import com.msa.chat_service.domain.member.dto.MemberInfoResponse;
import com.msa.chat_service.global.component.firebase.dto.request.FcmSubscribeRequest;
import com.msa.chat_service.global.component.firebase.dto.request.FcmTokenRequest;
import com.msa.chat_service.global.component.firebase.dto.request.FcmTopicRequest;
import com.msa.chat_service.global.component.firebase.service.FirebaseService;
import com.msa.chat_service.global.component.kafka.KafkaConstants;
import com.msa.chat_service.global.component.kafka.producer.KafkaProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final KafkaProducer kafkaProducer;
    private final FirebaseService firebaseService;
    private final ChatRoomMemberRepository chatRoomMemberRepository;

    @Override
    public List<ChatMessageResponse> selectChatMessages(Long chatRoomId, Long lastId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.NOT_EXIST_CHAT_ROOM));
        return chatMessageRepository.selectChatMessages(chatRoom, lastId);
    }



    // 단순히 메시지 전송 >> 단, chatRoomMember인지 확인
    @Override
    public void send(String topic, ChatMessageRequest request) {
        ChatRoom chatRoom = chatRoomRepository.findById(request.getChatRoomId())
                .orElseThrow(() -> new ChatException(ChatErrorCode.NOT_EXIST_CHAT_ROOM));

        // repo에 메시지 저장
        ChatMessage talkMessage = ChatMessage.createTalkMessage(request.getSenderId(), chatRoom, request.getContent());

        processMessage(talkMessage, request.getSenderId());
    }

    public void processMessage(ChatMessage chatMessage, Long memberId) {
        chatMessageRepository.save(chatMessage);
        MemberInfoResponse memberInfoResponse = getMemberInfo(memberId);
        if (memberInfoResponse == null){
            return;
        }
        ChatMessageResponse chatMessageResponse = ChatMessageResponse.of(chatMessage, memberInfoResponse);

        kafkaProducer.publish(KafkaConstants.KAFKA_TOPIC, chatMessageResponse);

        // topic : chat.room.{roomId}
//        firebaseService.sendMessageByTopic(
//                FcmTopicRequest.builder()
//                        .title(chatMessage.getChatRoom().getName())
//                        .body(chatMessage.getContent())
//                        .topicName("chat.room." + chatMessage.getChatRoom().getId())
//                        .build());
        List<Long> members = chatRoomMemberRepository.findAllByChatRoomId(chatMessage.getChatRoom().getId());
        for (Long member : members) {
            FcmTokenRequest fcmTokenRequest = new FcmTokenRequest(chatMessage.getChatRoom().getName(), chatMessage.getContent(), member);
            firebaseService.sendMessageByToken(fcmTokenRequest);
        }
    }

    private MemberInfoResponse getMemberInfo(Long memberId) {
        WebClient webClient = WebClient.create("http://localhost:9443");

        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/auth//member/community/{writerId}")
                            .build(memberId))
                    .retrieve()
                    .bodyToMono(MemberInfoResponse.class)
                    .block();
        } catch (Exception e) {
            e.printStackTrace();
            return null; // 실패 시 null 반환
        }
    }
}
