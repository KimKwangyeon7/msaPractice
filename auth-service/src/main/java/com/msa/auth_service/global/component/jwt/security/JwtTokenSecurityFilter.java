package com.msa.auth_service.global.component.jwt.security;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.msa.auth_service.global.common.dto.Message;
import com.msa.auth_service.global.component.jwt.JwtTokenProvider;
import com.msa.auth_service.global.component.jwt.exception.JwtTokenException;
import com.msa.auth_service.global.component.csrf.repository.CustomCsrfTokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * JWT 인증을 위한 커스텀 필터입니다.
 * HTTP 요청의 Authorization 헤더에서 JWT 액세스 토큰을 추출하고 검증하여,
 * 유효한 경우 Spring Security의 SecurityContext에 인증 정보를 설정합니다.
 */
@Slf4j
@RequiredArgsConstructor
public class JwtTokenSecurityFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;
    private static final String BEARER_PREFIX = "Bearer ";
    private final CustomCsrfTokenRepository customCsrfTokenRepository;
    /**
     * 요청에 대해 필터링 로직을 수행합니다.
     *
     * @param request     HTTP 요청 객체
     * @param response    HTTP 응답 객체
     * @param filterChain 필터 체인
     * @throws ServletException 서블릿 예외 발생 시
     * @throws IOException      입출력 예외 발생 시
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        // 요청에서 JWT 토큰과 CSRF 토큰 추출
        String accessToken = getJwtFrom(request);
        String csrfToken = request.getHeader("X-CSRF-TOKEN");
        System.out.println("Request URI: " + requestURI + ", CSRF Token: " + csrfToken + ", JWT Token: " +  accessToken);
        //log.info("클라이언트에서 수신한 CSRF 토큰: {}", csrfToken);

        if (csrfToken == null || accessToken == null) {
            System.out.println("CSRF 토큰 또는 JWT 토큰이 누락되었습니다.");
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF 토큰 또는 JWT 토큰이 누락되었습니다.");
            return;
        }

        // CSRF 토큰 검증
        if (!validateCsrfToken(request, csrfToken)) {
            log.error("CSRF 토큰이 유효하지 않습니다.");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(objectMapper.writeValueAsString(
                    Message.fail("INVALID_CSRF_TOKEN", "유효하지 않은 CSRF 토큰입니다.")
            ));
            return;
        }

        // JWT 토큰 검증
        if (StringUtils.hasText(accessToken)) {
            try {
                MemberLoginActive member = jwtTokenProvider.parseAccessToken(accessToken);
                log.info("회원 ID : {} - 요청 시도", member.id());
                SecurityContextHolder.getContext().setAuthentication(createAuthenticationToken(member));
            } catch (JwtTokenException e) {
                SecurityContextHolder.clearContext();
                sendError(response, e);
                return;
            }
        }

        // 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }

    private boolean validateCsrfToken(HttpServletRequest request, String csrfToken) {
        if (!StringUtils.hasText(csrfToken)) {
            log.error("CSRF 토큰이 요청에 포함되지 않았습니다.");
            return false;
        }

        // Redis 또는 저장소에서 저장된 CSRF 토큰 로드
        CsrfToken storedToken = customCsrfTokenRepository.loadToken(request);
        if (storedToken == null) {
            log.error("Redis에서 CSRF 토큰을 찾을 수 없습니다.");
            return false;
        }

        // 저장된 CSRF 토큰과 클라이언트가 보낸 토큰 비교
        log.info("Redis에서 가져온 CSRF 토큰: {}", storedToken.getToken());
        return csrfToken.equals(storedToken.getToken());
    }

    /**
     * 요청 헤더에서 JWT 토큰을 추출합니다.
     *
     * @param request HTTP 요청 객체
     * @return 추출된 JWT 토큰 문자열. 추출할 수 없는 경우 null 반환.
     */
    private String getJwtFrom(HttpServletRequest request) {
//        // HTTP 요청 헤더에서 'Authorization' 값을 가져옵니다.
//        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
//
//        // 요청 URI와 함께 추출된 액세스 토큰 값을 로깅합니다.
//        log.info("요청 : {} / 액세스 토큰 값 : {}", request.getRequestURI(), bearerToken);
//
//        // 헤더에 있는 토큰이 'Bearer '로 시작하는 경우, 해당 접두어를 제거하고 실제 토큰만 반환합니다.
//        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
//            return bearerToken.substring(BEARER_PREFIX.length());
//        }
//
//        // 'Bearer ' 접두어가 없거나 토큰 자체가 없는 경우 null을 반환합니다.
//        return null;
//        String csrfToken = request.getHeader("X-CSRF-TOKEN");
//        if (csrfToken != null) {
//            log.info("클라이언트에서 CSRF 토큰 수신: {}", csrfToken);
//        } else {
//            log.info("클라이언트 요청에 CSRF 토큰이 없습니다.");
//        }
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) { // JWT 쿠키 이름 확인
                    return cookie.getValue();
                }
            }
        }
        return null; // 쿠키에 JWT가 없는 경우
    }

    /**
     * 주어진 사용자 정보를 기반으로 JwtTokenAuthentication을 생성합니다.
     *
     * @param member 사용자 정보
     * @return 생성된 JwtAuthenticationToken 객체
     */
    private JwtTokenAuthentication createAuthenticationToken(MemberLoginActive member) {
        // JwtTokenAuthentication 객체를 생성하고, 사용자의 권한을 설정합니다.
        // 이 권한 정보는 Spring Security의 인증 과정에서 사용됩니다.
        return new JwtTokenAuthentication(member, "",
                List.of(new SimpleGrantedAuthority(member.role().name())));
    }

    /**
     * JWT 예외 발생 시 클라이언트에게 오류 응답을 보냅니다.
     *
     * @param response HTTP 응답 객체
     * @param e        발생한 JwtException
     * @throws IOException 입출력 예외 발생 시
     */
    private void sendError(HttpServletResponse response, JwtTokenException e) throws IOException {
        // 응답의 상태 코드를 예외에서 제공하는 상태 코드로 설정합니다.
        response.setStatus(e.getErrorCode().getHttpStatus().value());
        // 응답의 Content-Type을 'application/json'으로 설정합니다.
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // 응답 본문에 오류 메시지를 JSON 형식으로 작성하고 클라이언트에게 전송합니다.
        PrintWriter writer = response.getWriter();
        writer.write(objectMapper.writeValueAsString(Message.fail(e.getErrorCode().name(), e.getMessage())));
        writer.flush();
    }
}
