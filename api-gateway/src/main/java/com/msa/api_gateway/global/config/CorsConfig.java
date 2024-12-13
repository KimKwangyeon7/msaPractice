//package com.msa.api_gateway.global.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
//import org.springframework.web.server.adapter.ForwardedHeaderTransformer;
//
//@Configuration
//public class CorsConfig {
//
//    @Bean
//    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration config = new CorsConfiguration();
//        config.addAllowedOrigin("https://localhost:9443"); // 허용할 도메인
//        config.addAllowedMethod("*"); // 모든 HTTP 메서드 허용
//        config.addAllowedHeader("*"); // 모든 헤더 허용
//        config.setAllowCredentials(true); // 인증 정보 허용
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", config);
//        return source;
//    }
//
//    @Bean
//    public ForwardedHeaderTransformer forwardedHeaderTransformer() {
//        return new ForwardedHeaderTransformer();
//    }
//}
