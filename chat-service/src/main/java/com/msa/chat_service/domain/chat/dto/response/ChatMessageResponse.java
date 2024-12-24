package com.msa.chat_service.domain.chat.dto.response;

import com.msa.chat_service.domain.chat.entity.ChatMessage;
import com.msa.chat_service.domain.chat.entity.ChatRoom;
import com.msa.chat_service.domain.chat.entity.enums.MessageType;
import com.msa.chat_service.domain.member.dto.MemberInfoResponse;
import com.msa.chat_service.domain.member.entity.Member;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {
    private Long chatRoomId;
    private Long chatMessageId;
    private MessageType type;
    private String content;
    private LocalDateTime createdAt;
    private Long senderId;
    private String senderNickname;
    private String senderProfileImage;

    public ChatMessageResponse(Long chatRoomId, Long chatMessageId, MessageType type, String content, LocalDateTime createdAt, Long memberId){
        this.chatRoomId = chatRoomId;
        this.chatMessageId = chatMessageId;
        this.type = type;
        this.content = content;
        this.createdAt = createdAt;
        this.senderId = memberId;
    }

    public static ChatMessageResponse of(ChatMessage message, MemberInfoResponse sender) {
        ChatRoom chatRoom = message.getChatRoom();
        return ChatMessageResponse.builder()
                .chatMessageId(message.getId())
                .type(message.getType())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .senderId(sender.id())
                .senderNickname(sender.nickname())
                .senderProfileImage(sender.profileImage())
                .chatRoomId(chatRoom.getId())
                .build();
    }
}
