package com.msa.chat_service.domain.chat.repository;


import com.msa.chat_service.domain.chat.entity.ChatRoom;
import com.msa.chat_service.domain.chat.entity.ChatRoomMember;
import com.msa.chat_service.domain.member.entity.Member;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long>, CustomChatRoomMemberRepository {
    @Lock(LockModeType.OPTIMISTIC)
    Optional<ChatRoomMember> findLockByMemberIdAndChatRoom(Long member, ChatRoom chatRoom);
    Optional<ChatRoomMember> findByMemberIdAndChatRoom(Long member, ChatRoom chatRoom);

    void deleteByMemberIdAndChatRoom(Long member, ChatRoom chatRoom);

    boolean existsByChatRoom(ChatRoom chatRoom);

    boolean existsByMemberIdAndChatRoom(Long member, ChatRoom chatRoom);

    int countByChatRoom(ChatRoom chatRoom);

    @Query("SELECT c.memberId FROM ChatRoomMember c WHERE c.chatRoom.id = :chatRoomId")
    List<Long> findAllByChatRoomId(Long chatRoomId);
}
