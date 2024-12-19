package com.msa.chat_service.domain.chat.service;


import com.msa.chat_service.domain.chat.dto.request.CreateChatRoomRequest;
import com.msa.chat_service.domain.chat.dto.request.MyChatRoomListRequest;
import com.msa.chat_service.domain.chat.dto.response.ChatRoomListResponse;
import com.msa.chat_service.domain.chat.dto.response.ChatRoomResponse;
import com.msa.chat_service.domain.chat.dto.response.EnterChatRoomResponse;
import com.msa.chat_service.domain.chat.dto.response.MyChatRoomListResponse;

import java.util.List;

public interface ChatRoomService {
    List<ChatRoomListResponse> selectChatRooms(Long lastId);
    List<MyChatRoomListResponse> selectMyChatRooms(Long memberId, MyChatRoomListRequest request);
    ChatRoomResponse selectChatRoomDetail(Long chatRoomId);
    Long createChatRoom(Long memberId, CreateChatRoomRequest request);
    List<ChatRoomResponse> selectPopularChatRoom(String category);
    EnterChatRoomResponse enterChatRoom(Long memberId, Long chatRoomId);
    void exitChatRoom(Long memberId, Long chatRoomId);
}
