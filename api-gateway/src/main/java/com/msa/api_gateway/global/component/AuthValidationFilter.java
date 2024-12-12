package com.msa.api_gateway.global.component;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class AuthValidationFilter extends AbstractGatewayFilterFactory<AuthValidationFilter.Config> {

    private final WebClient webClient;

    public AuthValidationFilter(WebClient.Builder webClientBuilder) {
        super(Config.class);
        this.webClient = webClientBuilder.baseUrl("https://localhost:9443").build(); // 인증 서버 URL
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String csrfToken = exchange.getRequest().getHeaders().getFirst("X-CSRF-Token");
            String accessToken = exchange.getRequest().getHeaders().getFirst(HttpHeaders.COOKIE);

            // /auth로 시작하는 요청은 검증 없이 그대로 auth-server로 전달
            if (exchange.getRequest().getPath().toString().startsWith("/auth")) {
                return chain.filter(exchange);
            }

            // 헤더가 없는 경우 401 Unauthorized 응답
            if (csrfToken == null || accessToken == null) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            // WebClient를 통해 인증 서버에서 검증
            return webClient.post()
                    .uri("/auth/validate")
                    .header("X-CSRF-Token", csrfToken)
                    .header(HttpHeaders.COOKIE, accessToken)
                    .retrieve()
                    .onStatus(
                            HttpStatusCode::isError, // 에러 상태 확인
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
        // 필요 시 설정 추가 가능
    }
}
