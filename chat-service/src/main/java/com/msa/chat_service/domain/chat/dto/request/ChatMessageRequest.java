package com.msa.chat_service.domain.chat.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatMessageRequest {
    private Long chatRoomId;
    private String content;
    private Long senderId;
}
