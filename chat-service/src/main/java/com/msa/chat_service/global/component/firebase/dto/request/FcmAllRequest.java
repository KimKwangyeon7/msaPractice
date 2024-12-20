package com.msa.chat_service.global.component.firebase.dto.request;

import lombok.Builder;

@Builder
public record FcmAllRequest(
        String title,
        String body
) { }
