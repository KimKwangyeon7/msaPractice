package com.msa.auth_service.global.component.oauth.vendor.naver;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oauth.naver")
public record NaverOAuthProps(
        String redirectUri,
        String clientId,
        String clientSecret,
        String state
) {
}
