package com.msa.auth_service.global.component.oauth.vendor.google;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oauth.google")
public record GoogleOAuthProps(
        String redirectUri,
        String clientId,
        String clientSecret,
        String[] scope
) {
}
