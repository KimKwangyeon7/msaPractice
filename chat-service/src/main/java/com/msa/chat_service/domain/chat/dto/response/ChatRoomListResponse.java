package com.msa.chat_service.domain.chat.dto.response;


import com.msa.chat_service.domain.member.entity.enums.Category;

public record ChatRoomListResponse(
        Long chatRoomId,
        Category category,
        String name,
        int memberCount,
        int limit
) {
}
