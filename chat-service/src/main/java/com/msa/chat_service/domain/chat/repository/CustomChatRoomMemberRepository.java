package com.msa.chat_service.domain.chat.repository;


import com.msa.chat_service.domain.chat.dto.response.ChatRoomResponse;

import java.util.List;

public interface CustomChatRoomMemberRepository {
    List<ChatRoomResponse> selectPopularChatRoom(String category);
}
