package com.msa.chat_service.domain.chat.service;

import com.msa.chat_service.domain.chat.dto.request.CreateChatRoomRequest;
import com.msa.chat_service.domain.chat.dto.request.MyChatRoomListRequest;
import com.msa.chat_service.domain.chat.dto.response.ChatRoomListResponse;
import com.msa.chat_service.domain.chat.dto.response.ChatRoomResponse;
import com.msa.chat_service.domain.chat.dto.response.EnterChatRoomResponse;
import com.msa.chat_service.domain.chat.dto.response.MyChatRoomListResponse;
import com.msa.chat_service.domain.chat.entity.ChatMessage;
import com.msa.chat_service.domain.chat.entity.ChatRoom;
import com.msa.chat_service.domain.chat.entity.ChatRoomMember;
import com.msa.chat_service.domain.chat.exception.ChatErrorCode;
import com.msa.chat_service.domain.chat.exception.ChatException;
import com.msa.chat_service.domain.chat.repository.ChatMessageRepository;
import com.msa.chat_service.domain.chat.repository.ChatRoomMemberRepository;
import com.msa.chat_service.domain.chat.repository.ChatRoomRepository;
import com.msa.chat_service.domain.member.dto.MemberInfoResponse;
import com.msa.chat_service.domain.member.entity.Member;
import com.msa.chat_service.domain.member.entity.enums.Category;
import com.msa.chat_service.domain.member.entity.enums.MemberRole;
import com.msa.chat_service.global.component.firebase.dto.request.FcmSubscribeRequest;
import com.msa.chat_service.global.component.firebase.entity.DeviceToken;
import com.msa.chat_service.global.component.firebase.repository.DeviceTokenRepository;
import com.msa.chat_service.global.component.firebase.service.FirebaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ChatRoomServiceImpl implements ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatMessageService chatMessageService;

    @Override
    public List<ChatRoomListResponse> selectChatRooms(Long lastId) {
        return chatRoomRepository.selectChatRooms(lastId);
    }

    @Override
    public List<MyChatRoomListResponse> selectMyChatRooms(Long memberId, MyChatRoomListRequest request) {
        return chatRoomRepository.selectMyChatRooms(memberId, request);
    }

    @Override
    public Long createChatRoom(Long memberId, CreateChatRoomRequest request) {
        ChatRoom chatRoom = ChatRoom.builder()
                .category(Category.valueOf(request.category()))
                .name(request.name())
                .introduction(request.introduction())
                .limit(request.limit())
                .build();
        chatRoomRepository.save(chatRoom);

        chatRoomMemberRepository.save(ChatRoomMember.builder()
                .memberId(memberId)
                .chatRoom(chatRoom)
                .build());

        String topicName = "chat-room-" + chatRoom.getId();

        String token = getTokenByMemberId(memberId);

        FcmSubscribeRequest fcmSubscribeRequest = new FcmSubscribeRequest(token, topicName);


        callSubscribeByTopic(fcmSubscribeRequest);

        return chatRoom.getId();
    }

    private String getTokenByMemberId(Long memberId) {
        // 예제: WebClient 사용
        WebClient webClient = WebClient.create("http://localhost:9004");

        try {
            // REST API 호출
            return webClient.post()
                    .uri("/alarm/get/token") // FCM 구독 요청 엔드포인트
                    .bodyValue(memberId) // 요청 본문에 DTO 전달
                    .retrieve()
                    .bodyToMono(String.class) // 반환 값을 String으로 처리
                    .block(); // 동기화 호출
           // System.out.println("요청 성공: " + response);
        } catch (WebClientResponseException e) {
            // 응답에 대한 예외 처리
            System.err.println("요청 실패: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            throw new RuntimeException("요청 중 오류 발생", e);
        } catch (Exception e) {
            // 기타 예외 처리
            System.err.println("요청 중 알 수 없는 오류 발생: " + e.getMessage());
            throw new RuntimeException("요청 중 알 수 없는 오류 발생", e);
        }
    }

    private void callSubscribeByTopic(FcmSubscribeRequest request) {
        // 예제: WebClient 사용
        WebClient webClient = WebClient.create("http://localhost:9004");

        try {
            // REST API 호출
            String response = webClient.post()
                    .uri("/alarm/message/subscribe") // FCM 구독 요청 엔드포인트
                    .bodyValue(request) // 요청 본문에 DTO 전달
                    .retrieve()
                    .bodyToMono(String.class) // 반환 값을 String으로 처리
                    .block(); // 동기화 호출
            System.out.println("구독 성공: " + response);
        } catch (WebClientResponseException e) {
            // 응답에 대한 예외 처리
            System.err.println("구독 요청 실패: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            throw new RuntimeException("FCM 구독 요청 중 오류 발생", e);
        } catch (Exception e) {
            // 기타 예외 처리
            System.err.println("FCM 구독 요청 중 알 수 없는 오류 발생: " + e.getMessage());
            throw new RuntimeException("FCM 구독 요청 중 알 수 없는 오류 발생", e);
        }
    }

    @Override
    public ChatRoomResponse selectChatRoomDetail(Long chatRoomId) {
        return chatRoomRepository.selectChatRoomDetail(chatRoomId);
    }

    @Override
    public EnterChatRoomResponse enterChatRoom(Long memberId, String nickname,Long chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.NOT_EXIST_CHAT_ROOM));
        int memberCount = chatRoomMemberRepository.countByChatRoom(chatRoom);

        if (!chatRoomMemberRepository.existsByMemberIdAndChatRoom(memberId, chatRoom)) {
            // 처음 입장한 경우 환영메시지 생성 후 DB 저장, 메시지 전송
            if (chatRoom.isFull(memberCount)) {
                throw new ChatException(ChatErrorCode.FULL_CHAT_ROOM);
            }
            // 토픽 구독
            String topicName = "chat.room." + chatRoomId;
            String token = getTokenByMemberId(memberId);
            if (token == null) {
                // 사용자가 알림 권한을 허용하지 않아 토큰이 없는 경우 처리
                log.warn("Member with ID {} has no FCM token. Skipping topic subscription.", memberId);
            } else {
                // 토큰이 있는 경우에만 토픽 구독
                FcmSubscribeRequest fcmSubscribeRequest = new FcmSubscribeRequest(token, topicName);
                callSubscribeByTopic(fcmSubscribeRequest);
            }

            chatRoomMemberRepository.save(ChatRoomMember
                    .builder()
                    .memberId(memberId)
                    .chatRoom(chatRoom)
                    .build());
            ChatMessage enterMessage = ChatMessage.createEnterMessage(memberId, nickname, chatRoom);
            chatMessageService.processMessage(enterMessage, memberId);
        }

        return new EnterChatRoomResponse(chatRoomId);
    }

    @Override
    public List<ChatRoomResponse> selectPopularChatRoom(String category) {
        // 채팅방 인원 많은 순으로 조회
        return chatRoomMemberRepository.selectPopularChatRoom(category);
    }

    @Override
    public void exitChatRoom(Long memberId, String nickname, Long chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.NOT_EXIST_CHAT_ROOM));

        // 채팅방 탈퇴 메시지 전송
        ChatMessage exitMessage = ChatMessage.createExitMessage(memberId, nickname, chatRoom);

        chatMessageService.processMessage(exitMessage, memberId);

        // 구성원 제거
        chatRoomMemberRepository.deleteByMemberIdAndChatRoom(memberId, chatRoom);
        // 토픽 구독 취소
        String topicName = "chat.room." + chatRoomId;
        String token = getTokenByMemberId(memberId);
        FcmSubscribeRequest fcmSubscribeRequest = new FcmSubscribeRequest(token, topicName);

        callUnsubscribeByTopic(fcmSubscribeRequest);

        if (!chatRoomMemberRepository.existsByChatRoom(chatRoom)) {
            // 채팅 메시지 삭제
            chatMessageRepository.deleteByChatRoom(chatRoom);
            // 채팅방 삭제
            chatRoomRepository.delete(chatRoom);
        }
    }

    private void callUnsubscribeByTopic(FcmSubscribeRequest request) {
        // 예제: WebClient 사용
        WebClient webClient = WebClient.create("http://localhost:9004");

        try {
            // REST API 호출
            String response = webClient.post()
                    .uri("/alarm/message/unsubscribe") // FCM 구독 요청 엔드포인트
                    .bodyValue(request) // 요청 본문에 DTO 전달
                    .retrieve()
                    .bodyToMono(String.class) // 반환 값을 String으로 처리
                    .block(); // 동기화 호출
            System.out.println("구독 취소 성공: " + response);
        } catch (WebClientResponseException e) {
            // 응답에 대한 예외 처리
            System.err.println("구독 취소 요청 실패: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            throw new RuntimeException("FCM 구독 취소 요청 중 오류 발생", e);
        } catch (Exception e) {
            // 기타 예외 처리
            System.err.println("FCM 구독 취소 요청 중 알 수 없는 오류 발생: " + e.getMessage());
            throw new RuntimeException("FCM 취소 구독 요청 중 알 수 없는 오류 발생", e);
        }
    }

}
