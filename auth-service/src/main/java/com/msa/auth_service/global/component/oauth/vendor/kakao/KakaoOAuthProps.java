package com.msa.auth_service.global.component.oauth.vendor.kakao;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oauth.kakao")
public record KakaoOAuthProps(
        String redirectUri,
        String clientId,
        String clientSecret,
        String[] scope
) {
}

