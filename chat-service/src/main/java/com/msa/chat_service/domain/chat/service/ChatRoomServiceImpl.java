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
import com.msa.chat_service.domain.member.entity.Member;
import com.msa.chat_service.domain.member.entity.enums.Category;
import com.msa.chat_service.domain.member.entity.enums.MemberRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

        return chatRoom.getId();
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

        if (!chatRoomMemberRepository.existsByChatRoom(chatRoom)) {
            // 채팅 메시지 삭제
            chatMessageRepository.deleteByChatRoom(chatRoom);
            // 채팅방 삭제
            chatRoomRepository.delete(chatRoom);
        }
    }
}
