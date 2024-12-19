package com.msa.chat_service.domain.chat.entity;

import com.msa.chat_service.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoomMember {
    @Id
    @Comment("채팅방 구성 아이디")
    @Column(columnDefinition = "INT UNSIGNED")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("구성원 아이디")
    @Column(columnDefinition = "INT UNSIGNED", nullable = false)
    private Long memberId;

    @Comment("채팅방 아이디")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;
}
