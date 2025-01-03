package com.msa.alarm_service.domain.member.entity;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "jwt")
public record JwtTokenPropsInfo(
        String accessKey,
        Duration accessExpiration,
        String refreshKey,
        Duration refreshExpiration
) {
}
