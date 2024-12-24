package com.msa.api_gateway.global.component;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.stream.Collectors;

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

            // 쿼리 파라미터에서 CSRF 토큰 확인
            ServerWebExchange updatedExchange;
            if (csrfToken == null) {
                csrfToken = exchange.getRequest().getQueryParams().getFirst("_csrf");
                if (csrfToken != null) {
                    // 헤더에 CSRF 토큰 추가하고 쿼리 파라미터 제거
                    ServerHttpRequest updatedRequest = exchange.getRequest().mutate()
                            .header("X-CSRF-Token", csrfToken) // 헤더에 CSRF 토큰 추가
                            .uri(removeQueryParam(exchange.getRequest().getURI(), "_csrf")) // 쿼리 파라미터 제거
                            .build();
                    // 기존 요청을 교체
                    updatedExchange = exchange.mutate().request(updatedRequest).build();
                } else {
                    updatedExchange = exchange;
                }
            } else {
                updatedExchange = exchange;
            }

            // auth 경로는 인증 검증 생략
            if (updatedExchange.getRequest().getPath().toString().startsWith("/auth")) {
                return chain.filter(updatedExchange);
            }

            // GET 요청인 경우 필터를 생략
            if (HttpMethod.GET.equals(updatedExchange.getRequest().getMethod())) {
                return chain.filter(updatedExchange);
            }

            // CSRF 토큰 또는 Access Token이 없는 경우 처리
            if (csrfToken == null || accessToken == null) {
                System.out.println("csrfToken: " + csrfToken + " accessToken: " + accessToken);
                updatedExchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return updatedExchange.getResponse().setComplete();
            }

            // /auth/validate로 검증 요청
            String finalCsrfToken = csrfToken; // 람다 내부에서 사용하기 위해 final로 선언
            return webClient.post()
                    .uri("/auth/validate")
                    .header("X-CSRF-Token", finalCsrfToken)
                    .header(HttpHeaders.COOKIE, accessToken)
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            response -> Mono.error(new RuntimeException("Invalid Token"))
                    )
                    .toBodilessEntity()
                    .flatMap(response -> chain.filter(updatedExchange)) // 수정된 Exchange 사용
                    .onErrorResume(e -> {
                        updatedExchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return updatedExchange.getResponse().setComplete();
                    });
        };
    }

    // Helper Method to Remove Query Parameters
    private URI removeQueryParam(URI uri, String paramName) {
        String query = uri.getQuery();
        if (query == null || !query.contains(paramName)) {
            return uri;
        }

        String updatedQuery = Arrays.stream(query.split("&"))
                .filter(param -> !param.startsWith(paramName + "="))
                .collect(Collectors.joining("&"));

        try {
            return new URI(
                    uri.getScheme(),
                    uri.getAuthority(),
                    uri.getPath(),
                    updatedQuery.isEmpty() ? null : updatedQuery,
                    uri.getFragment()
            );
        } catch (URISyntaxException e) {
            throw new RuntimeException("Failed to remove query parameter", e);
        }
    }

    public static class Config {
        // 설정 클래스 (필요시 확장 가능)
    }
}