package com.msa.chat_service.domain.chat.dto.response;


import com.msa.chat_service.domain.member.entity.enums.Category;

public record ChatRoomResponse(
        Long chatRoomId,
        Category category,
        String name,
        String introduction,
        int memberCount,
        int limit
) {
}
