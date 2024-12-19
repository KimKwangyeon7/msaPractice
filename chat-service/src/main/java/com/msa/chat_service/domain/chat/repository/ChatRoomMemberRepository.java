package com.msa.chat_service.domain.chat.repository;


import com.msa.chat_service.domain.chat.entity.ChatRoom;
import com.msa.chat_service.domain.chat.entity.ChatRoomMember;
import com.msa.chat_service.domain.member.entity.Member;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long>, CustomChatRoomMemberRepository {
    @Lock(LockModeType.OPTIMISTIC)
    Optional<ChatRoomMember> findLockByMemberAndChatRoom(Member member, ChatRoom chatRoom);
    Optional<ChatRoomMember> findByMemberAndChatRoom(Member member, ChatRoom chatRoom);

    void deleteByMemberAndChatRoom(Member member, ChatRoom chatRoom);

    boolean existsByChatRoom(ChatRoom chatRoom);

    boolean existsByMemberAndChatRoom(Member member, ChatRoom chatRoom);

    int countByChatRoom(ChatRoom chatRoom);
}
