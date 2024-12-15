package com.msa.api_gateway.global.component;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class AuthValidationFilter extends AbstractGatewayFilterFactory<AuthValidationFilter.Config> {

    private final WebClient webClient;

    public AuthValidationFilter(WebClient.Builder webClientBuilder) {
        super(Config.class);
        this.webClient = webClientBuilder.baseUrl("http://localhost:8443").build(); // 인증 서버 URL
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String csrfToken = exchange.getRequest().getHeaders().getFirst("X-CSRF-Token");
            String accessToken = exchange.getRequest().getHeaders().getFirst(HttpHeaders.COOKIE);

            // auth 경로는 인증 검증 생략
            if (exchange.getRequest().getPath().toString().startsWith("/auth")) {
                return chain.filter(exchange);
            }

            if (csrfToken == null || accessToken == null) {
                System.out.println("csrfToken: " + csrfToken + "accessToken: " + accessToken);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            return webClient.post()
                    .uri("/auth/validate")
                    .header("X-CSRF-Token", csrfToken)
                    .header(HttpHeaders.COOKIE, accessToken)
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            response -> Mono.error(new RuntimeException("Invalid Token"))
                    )
                    .toBodilessEntity()
                    .flatMap(response -> chain.filter(exchange))
                    .onErrorResume(e -> {
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    });
        };
    }

    public static class Config {
        // 설정 클래스 (필요시 확장 가능)
    }
}