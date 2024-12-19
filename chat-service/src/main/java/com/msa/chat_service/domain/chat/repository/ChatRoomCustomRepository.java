package com.msa.chat_service.domain.chat.repository;


import com.msa.chat_service.domain.chat.dto.request.MyChatRoomListRequest;
import com.msa.chat_service.domain.chat.dto.response.ChatRoomListResponse;
import com.msa.chat_service.domain.chat.dto.response.ChatRoomResponse;
import com.msa.chat_service.domain.chat.dto.response.MyChatRoomListResponse;

import java.util.List;

public interface ChatRoomCustomRepository {
    List<ChatRoomListResponse> selectChatRooms(Long lastId);
    List<MyChatRoomListResponse> selectMyChatRooms(Long memberId, MyChatRoomListRequest request);
    ChatRoomResponse selectChatRoomDetail(Long chatRoomId);
}
