package com.msa.chat_service.global.component.firebase.dto.request;

import lombok.Builder;

@Builder
public record FcmTokenRequest(
        String title,
        String body,
        Long memberId,
        Long chatRoomId
) { }
