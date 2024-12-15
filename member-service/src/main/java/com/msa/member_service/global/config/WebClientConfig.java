package com.msa.member_service.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient authServiceWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("http://localhost:9443") // auth-service의 기본 URL
                .build();
    }
}