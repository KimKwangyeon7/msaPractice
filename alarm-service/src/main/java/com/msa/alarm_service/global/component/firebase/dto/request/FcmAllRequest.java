package com.msa.alarm_service.global.component.firebase.dto.request;

import lombok.Builder;

@Builder
public record FcmAllRequest(
        String title,
        String body
) { }
