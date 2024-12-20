package com.msa.chat_service.global.component.firebase.dto.request;

import lombok.Builder;

@Builder
public record FcmTopicRequest(
        String title,
        String body,
        String topicName
) { }
