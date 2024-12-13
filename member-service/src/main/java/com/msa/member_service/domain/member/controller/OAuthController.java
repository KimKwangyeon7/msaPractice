//package com.msa.member_service.domain.member.controller;
//
//
//import com.study.springStudy.domain.member.dto.MemberLoginResponse;
//import com.study.springStudy.domain.member.service.OAuthService;
//import com.study.springStudy.global.common.dto.Message;
//import com.study.springStudy.global.component.oauth.vendor.enums.OAuthDomain;
//import jakarta.servlet.http.Cookie;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/api/v1/oauth")
//public class OAuthController {
//    private final OAuthService oAuthService;
//
//
//    @GetMapping("/{oAuthDomain}")
//    public ResponseEntity<Message<String>> provideAuthCodeRequestUrlOAuth(@PathVariable OAuthDomain oAuthDomain) {
//        String redirectUrl = oAuthService.provideAuthCodeRequestUrlOAuth(oAuthDomain);
//        return ResponseEntity.ok().body(Message.success(redirectUrl));
//    }
//
//
//    @GetMapping("/{oAuthDomain}/login")
//    public ResponseEntity<Message<MemberLoginResponse>> loginOAuth(@PathVariable("oAuthDomain") OAuthDomain oAuthDomain,
//                                                                   @RequestParam("code") String authCode,
//                                                                   HttpServletResponse response) {
//        MemberLoginResponse loginResponse = oAuthService.loginOAuth(oAuthDomain, authCode);
//        // JWT 토큰을 쿠키에 저장
//        Cookie accessTokenCookie = new Cookie("accessToken", loginResponse.tokenInfo().accessToken());
//        accessTokenCookie.setPath("/");
//        accessTokenCookie.setMaxAge(25200); // 4200분(25200초)으로 설정 (25200)
//        response.addCookie(accessTokenCookie);
//        return ResponseEntity.ok().body(Message.success(loginResponse));
//    }
//
//}
