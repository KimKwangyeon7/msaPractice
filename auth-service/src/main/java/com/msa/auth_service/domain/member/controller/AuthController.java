package com.msa.auth_service.domain.member.controller;


import com.msa.auth_service.domain.member.dto.MemberLoginRequest;
import com.msa.auth_service.domain.member.dto.MemberLoginResponse;
import com.msa.auth_service.domain.member.dto.MemberSignupRequest;
import com.msa.auth_service.domain.member.service.MemberService;
import com.msa.auth_service.global.common.dto.Message;
import com.msa.auth_service.global.component.jwt.security.MemberLoginActive;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final MemberService memberService;
    private final RedisTemplate<String, String> redisTemplate;


    @PostMapping("/validate")
    public ResponseEntity<?> validateToken() {
        return ResponseEntity.ok("Token validated successfully");
    }

    @GetMapping("/signup")
    public String signUpPage() {
        return "signup";
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody MemberSignupRequest signupRequest) {
        memberService.signupMember(signupRequest);
        return ResponseEntity.ok(Map.of("redirectUrl", "/auth/login", "message", Message.success()));
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login"; // JSP 파일 경로: /WEB-INF/views/login.jsp
    }


    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpServletResponse response,
                        RedirectAttributes redirectAttributes) {
        try {
            // 로그인 요청 처리
            MemberLoginRequest loginRequest = new MemberLoginRequest(username, password);
            MemberLoginResponse memberLoginResponse = memberService.loginMember(loginRequest, response);

            // CSRF 토큰 생성 후 RedirectAttributes에 저장
            String csrfToken = response.getHeader("X-CSRF-TOKEN");
            redirectAttributes.addFlashAttribute("csrfToken", csrfToken);
            redirectAttributes.addFlashAttribute("message", Message.success(memberLoginResponse)); // 메시지 추가
            return "redirect:/auth/main";
        } catch (Exception e) {
            // 로그인 실패 시 에러 메시지 전달
            redirectAttributes.addFlashAttribute("error", "Invalid username or password");
            return "redirect:/auth/login";
        }
    }

    @GetMapping("/main")
    public String mainPage(@ModelAttribute("csrfToken") String csrfToken, @ModelAttribute("message") Message<MemberLoginResponse> message, Model model) {
        model.addAttribute("csrfToken", csrfToken);
        model.addAttribute("message", message);
        System.out.println("csrfToken: " + csrfToken);

        return "main"; // JSP 파일 경로: /WEB-INF/views/main.jsp
    }


    @PostMapping("/logout")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    public ResponseEntity<?> logout(@AuthenticationPrincipal MemberLoginActive loginActive,
                                                 HttpServletResponse response, HttpSession session) throws IOException {
        // 요청 처리 로직
        memberService.logoutMember(loginActive.email());
        Cookie accessTokenCookie = new Cookie("accessToken", null);
        accessTokenCookie.setMaxAge(0);
        accessTokenCookie.setPath("/");
        response.addCookie(accessTokenCookie);

        // SecurityContext 초기화
        SecurityContextHolder.clearContext();

        // 세션 무효화
        session.invalidate();

        // 클라이언트로 리다이렉트 URL 전달
        return ResponseEntity.ok(Map.of("redirectUrl", "/auth/login", "message", "Logout successful"));
    }
}
