//package com.msa.api_gateway.global.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
//import org.springframework.security.config.web.server.ServerHttpSecurity;
//import org.springframework.security.web.server.SecurityWebFilterChain;
//
//@Configuration
//@EnableWebFluxSecurity
//public class SecurityConfig {
//
//    @Bean
//    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
//        return http
//                .csrf(ServerHttpSecurity.CsrfSpec::disable) // CSRF 비활성화
//                .authorizeExchange(exchange -> exchange
//                        .pathMatchers("/auth/**").permitAll() // auth 경로는 인증 없이 접근 허용
//                        .anyExchange().authenticated() // 다른 요청은 인증 필요
//                )
//                .build();
//    }
//}
//
