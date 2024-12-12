package com.msa.auth_service.global.component.csrf.repository;

import com.msa.auth_service.global.component.jwt.JwtTokenPropsInfo;
import com.msa.auth_service.global.component.jwt.exception.JwtTokenErrorCode;
import com.msa.auth_service.global.component.jwt.exception.JwtTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * CustomCsrfTokenRepository
 * - Redis를 사용하여 CSRF 토큰을 저장, 조회, 삭제
 * - JWT에서 이메일 추출 후 Redis 키로 사용
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class CustomCsrfTokenRepository implements CsrfTokenRepository {

    private static final String CSRF_HEADER_NAME = "X-CSRF-TOKEN"; // 클라이언트 요청 헤더 이름
    private static final String CSRF_PARAMETER_NAME = "_csrf"; // 요청 파라미터 이름
    private static final String CSRF_REDIS_KEY_PREFIX = "csrfToken::"; // Redis 키 접두사
    private static final String JWT_COOKIE_NAME = "accessToken"; // JWT 쿠키 이름
    private static final long TOKEN_TTL = 100; // 토큰 유효 기간 (분)

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtTokenPropsInfo tokenPropsInfo; // JWT 검증을 위한 키


    @Override
    public CsrfToken generateToken(HttpServletRequest request) {
        // UUID를 사용해 고유한 CSRF 토큰 생성
        String token = UUID.randomUUID().toString();
        return new DefaultCsrfToken(CSRF_HEADER_NAME, CSRF_PARAMETER_NAME, token);
    }

    @Override
    public void saveToken(CsrfToken token, HttpServletRequest request, HttpServletResponse response) {
        if (token == null) {
            // 토큰이 null인 경우 Redis에서 해당 키 삭제
            String email = extractEmailFromJwtCookie(request);
            if (email != null) {
                redisTemplate.delete(CSRF_REDIS_KEY_PREFIX + email);
            }
        } else {
            // JWT에서 이메일 추출
            String email = extractEmailFromJwtCookie(request);
            if (email != null) {
                // CSRF 토큰을 Redis에 저장
                redisTemplate.opsForValue().set(
                        CSRF_REDIS_KEY_PREFIX + email, // Redis 키
                        token.getToken(), // 저장할 토큰 값
                        TOKEN_TTL, // 유효 시간
                        TimeUnit.MINUTES // 단위: 분
                );

                // CSRF 토큰을 응답 헤더에 추가
                response.setHeader(CSRF_HEADER_NAME, token.getToken());
            }
        }
    }

    public void issueAndSaveCsrfToken(String email, HttpServletResponse response){
        CsrfToken token = new DefaultCsrfToken(CSRF_HEADER_NAME, CSRF_PARAMETER_NAME, UUID.randomUUID().toString());
        // CSRF 토큰을 Redis에 저장
        redisTemplate.opsForValue().set(
                CSRF_REDIS_KEY_PREFIX + email, // Redis 키
                token.getToken(), // 저장할 토큰 값
                TOKEN_TTL, // 유효 시간
                TimeUnit.MINUTES // 단위: 분
        );

        // CSRF 토큰을 응답 헤더에 추가
        response.setHeader(CSRF_HEADER_NAME, token.getToken());
    }

    @Override
    public CsrfToken loadToken(HttpServletRequest request) {

        String csrfToken = request.getHeader("X-CSRF-TOKEN");
        if (csrfToken != null) {
            log.info("클라이언트에서 CSRF 토큰 수신: {}", csrfToken);
        } else {
            log.info("클라이언트 요청에 CSRF 토큰이 없습니다.");
        }

        // JWT에서 이메일 추출
        String email = extractEmailFromJwtCookie(request);
        log.info("csrf 토큰 검증하기! 아메일: {} - 요청 시도", email);
        if (email != null) {
            // Redis에서 CSRF 토큰 조회
            String tokenValue = redisTemplate.opsForValue().get(CSRF_REDIS_KEY_PREFIX + email);
            CsrfToken token = new DefaultCsrfToken(CSRF_HEADER_NAME, CSRF_PARAMETER_NAME, tokenValue);
            log.info("생성된 DefaultCsrfToken 객체: {} {} {}", token, token.getHeaderName(), token.getToken());
            return token;
        }
        return null;
    }

    /**
     * JWT 쿠키에서 이메일을 추출합니다.
     *
     * @param request HTTP 요청 객체
     * @return 이메일 (없으면 null)
     */
    private String extractEmailFromJwtCookie(HttpServletRequest request) {
        String jwt = extractJwtFromCookies(request);
        Claims payload;

        try {
            // 토큰을 파싱하여 payload를 반환합니다. 이 과정에서 토큰의 무결성과 유효성이 검증됩니다.
            payload = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(tokenPropsInfo.accessKey().getBytes()))
                    .build()
                    .parseSignedClaims(jwt).getPayload();
        } catch (ExpiredJwtException e) {
            // 토큰 만료 예외 처리
            throw new JwtTokenException(JwtTokenErrorCode.EXPIRED_TOKEN);
        } catch (MalformedJwtException | SecurityException | IllegalArgumentException e) {
            // 토큰 형식 불일치 예외 처리
            throw new JwtTokenException(JwtTokenErrorCode.INVALID_TOKEN);
        }
        return payload.get("email", String.class);
    }


    /**
     * HTTP 요청의 쿠키에서 JWT 토큰을 추출합니다.
     *
     * @param request HTTP 요청 객체
     * @return JWT 토큰 (없으면 null)
     */
    private String extractJwtFromCookies(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (JWT_COOKIE_NAME.equals(cookie.getName())) { // JWT 쿠키 이름 확인
                    log.info("쿠키에서 JWT 토큰 가져오기 : {}  - 요청 시도", cookie.getValue());
                    return cookie.getValue();
                }
            }
        }
        return null; // 쿠키에 JWT가 없는 경우
    }

    public void delete(String email) {
        redisTemplate.delete(CSRF_REDIS_KEY_PREFIX + email);
    }
}
