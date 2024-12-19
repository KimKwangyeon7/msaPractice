package com.msa.chat_service.domain.chat.service;

import com.msa.chat_service.domain.chat.dto.request.ChatMessageRequest;
import com.msa.chat_service.domain.chat.dto.response.ChatMessageResponse;
import com.msa.chat_service.domain.chat.entity.ChatMessage;
import com.msa.chat_service.domain.chat.entity.ChatRoom;
import com.msa.chat_service.domain.chat.exception.ChatErrorCode;
import com.msa.chat_service.domain.chat.exception.ChatException;
import com.msa.chat_service.domain.chat.repository.ChatMessageRepository;
import com.msa.chat_service.domain.chat.repository.ChatRoomRepository;
import com.msa.chat_service.domain.member.entity.Member;
import com.msa.chat_service.domain.member.exception.MemberErrorCode;
import com.msa.chat_service.domain.member.exception.MemberException;
import com.msa.chat_service.global.component.kafka.KafkaConstants;
import com.msa.chat_service.global.component.kafka.producer.KafkaProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    //private final MemberRepository memberRepository;
    private final KafkaProducer kafkaProducer;
    //private final FirebaseService firebaseService;

    @Override
    public List<ChatMessageResponse> selectChatMessages(Long chatRoomId, Long lastId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.NOT_EXIST_CHAT_ROOM));
        return chatMessageRepository.selectChatMessages(chatRoom, lastId);
    }



    // 단순히 메시지 전송 >> 단, chatRoomMember인지 확인
    @Override
    public void send(String topic, ChatMessageRequest request) {
//        Member member = memberRepository.findById(request.getSenderId())
//                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER));
        Member member = null;

        ChatRoom chatRoom = chatRoomRepository.findById(request.getChatRoomId())
                .orElseThrow(() -> new ChatException(ChatErrorCode.NOT_EXIST_CHAT_ROOM));

        // repo에 메시지 저장
        ChatMessage talkMessage = ChatMessage.createTalkMessage(member, chatRoom, request.getContent());

        processMessage(talkMessage);
    }

    public void processMessage(ChatMessage chatMessage) {
        chatMessageRepository.save(chatMessage);
        ChatMessageResponse chatMessageResponse = ChatMessageResponse.of(chatMessage);

        kafkaProducer.publish(KafkaConstants.KAFKA_TOPIC, chatMessageResponse);

        // topic : chat.room.{roomId}
//        firebaseService.sendMessageByTopic(
//                FcmTopicRequest.builder()
//                        .title(chatMessage.getChatRoom().getName())
//                        .body(chatMessage.getContent())
//                        .topicName("chat.room." + chatMessage.getChatRoom().getId())
//                        .build());
    }
}
