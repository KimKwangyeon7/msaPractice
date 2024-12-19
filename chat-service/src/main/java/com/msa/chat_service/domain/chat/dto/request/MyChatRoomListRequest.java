package com.msa.chat_service.domain.chat.dto.request;

public record MyChatRoomListRequest(
        String keyword,
        Long lastId
) {
}
