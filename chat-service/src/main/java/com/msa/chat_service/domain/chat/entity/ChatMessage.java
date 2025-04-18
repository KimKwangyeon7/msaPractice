package com.msa.chat_service.domain.chat.entity;

import com.msa.chat_service.domain.chat.entity.enums.MessageType;
import com.msa.chat_service.domain.member.entity.Member;
import com.msa.chat_service.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage extends BaseEntity {
    @Id
    @Comment("채팅 내역 아이디")
    @Column(columnDefinition = "INT UNSIGNED")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("작성자 아이디")
    @Column(columnDefinition = "INT UNSIGNED", nullable = false)
    private Long memberId;

    @Comment("채팅방 아이디")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @Comment("채팅 내용 종류")
    @Enumerated(EnumType.STRING)
    private MessageType type;

    @Comment("채팅 내용")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    public static ChatMessage createExitMessage(Long memberId, String nickname, ChatRoom chatRoom) {
        return ChatMessage.builder()
                .memberId(memberId)
                .chatRoom(chatRoom)
                .type(MessageType.EXIT)
                .content(nickname + "님이 나가셨습니다.")
                .build();
    }

    public static ChatMessage createEnterMessage(Long memberId, String nickname, ChatRoom chatRoom) {
        return ChatMessage.builder()
                .memberId(memberId)
                .chatRoom(chatRoom)
                .type(MessageType.ENTER)
                .content(nickname + "님이 입장하셨습니다.")
                .build();
    }

    public static ChatMessage createTalkMessage(Long memberId, ChatRoom chatRoom, String content) {
        return ChatMessage.builder()
                .memberId(memberId)
                .chatRoom(chatRoom)
                .type(MessageType.TALK)
                .content(content)
                .build();
    }
}
