package com.msa.chat_service.domain.chat.repository;


import com.msa.chat_service.domain.chat.dto.response.ChatMessageResponse;
import com.msa.chat_service.domain.chat.entity.ChatRoom;
import java.util.List;

public interface ChatMessageCustomRepository {
    List<ChatMessageResponse> selectChatMessages(ChatRoom chatRoom, Long lastId);
}
