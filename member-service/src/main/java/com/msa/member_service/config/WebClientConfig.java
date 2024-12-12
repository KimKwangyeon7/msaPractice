package com.msa.member_service.config;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient authServiceWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("http://auth-service") // auth-service의 기본 URL
                .build();
    }
}