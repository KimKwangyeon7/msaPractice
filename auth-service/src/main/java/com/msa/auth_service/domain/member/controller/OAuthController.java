package com.msa.auth_service.domain.member.controller;


import com.msa.auth_service.domain.member.dto.MemberLoginResponse;
import com.msa.auth_service.domain.member.service.OAuthService;
import com.msa.auth_service.global.common.dto.Message;
import com.msa.auth_service.global.component.oauth.vendor.enums.OAuthDomain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;


@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/oauth")
public class OAuthController {
    private final OAuthService oAuthService;
    private final RedisTemplate<String, String> redisTemplate;

    @GetMapping("/{oAuthDomain}")
    public ResponseEntity<?> provideAuthCodeRequestUrlOAuth(@PathVariable OAuthDomain oAuthDomain) {
        String redirectUrl = oAuthService.provideAuthCodeRequestUrlOAuth(oAuthDomain);
        return ResponseEntity.ok(Map.of("redirectUrl", redirectUrl));
    }


    @GetMapping("/{oAuthDomain}/login")
    public void loginOAuth(@PathVariable("oAuthDomain") OAuthDomain oAuthDomain,
                                                                   @RequestParam("code") String authCode,
                                                                   HttpServletRequest request,
                                                                   HttpServletResponse response) throws IOException {
        MemberLoginResponse loginResponse = oAuthService.loginOAuth(oAuthDomain, authCode, response);
        storeUserInRedis(loginResponse);
        // JWT 토큰을 쿠키에 저장
        Cookie accessTokenCookie = new Cookie("accessToken", loginResponse.tokenInfo().accessToken());
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(25200); // 4200분(25200초)으로 설정 (25200)
        // HttpOnly 설정: JavaScript에서 쿠키에 접근하지 못하도록 제한
        accessTokenCookie.setHttpOnly(true);
        // Secure 설정: HTTPS에서만 전송되도록 제한 (HTTPS를 사용하지 않는 경우 주석 처리 가능)
        accessTokenCookie.setSecure(false);
        // SameSite 설정: 요청의 출처를 제한하여 CSRF 공격을 방지
        // SameSite 설정은 Java 11 이상에서 지원됩니다.
        accessTokenCookie.setAttribute("SameSite", "None");
        response.addCookie(accessTokenCookie);

        // CSRF 토큰과 이메일을 세션에 저장
        String csrfToken = response.getHeader("X-CSRF-TOKEN");
        String email = loginResponse.memberInfo().email();
        HttpSession session = request.getSession(true);
        session.setAttribute("csrfToken", csrfToken);
        session.setAttribute("email", email);

        // **리다이렉트 처리**
        response.sendRedirect("http://localhost:8443/auth/main");
    }

    public void storeUserInRedis(MemberLoginResponse memberLoginResponse) {
        String redisKey = "memberInfo::" + memberLoginResponse.memberInfo().email();
        redisTemplate.opsForValue().set(redisKey, String.valueOf(memberLoginResponse.memberInfo()), Duration.ofHours(24));
    }

}
