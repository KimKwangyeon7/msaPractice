package com.msa.alarm_service.global.component.firebase.dto.request;

import lombok.Builder;

@Builder
public record FcmTopicRequest(
        String title,
        String body,
        String topicName
) { }
