package com.msa.chat_service.domain.chat.service;


import com.msa.chat_service.domain.chat.dto.request.ChatMessageRequest;
import com.msa.chat_service.domain.chat.dto.response.ChatMessageResponse;
import com.msa.chat_service.domain.chat.entity.ChatMessage;

import java.util.List;

public interface ChatMessageService {
    List<ChatMessageResponse> selectChatMessages(Long chatRoomId, Long lastId);
    void send(String topic, ChatMessageRequest request);
    void processMessage(ChatMessage chatMessage, Long memberId);
}
