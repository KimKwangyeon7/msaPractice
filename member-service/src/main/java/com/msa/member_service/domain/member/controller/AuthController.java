package com.msa.member_service.domain.member.controller;

import com.study.springStudy.domain.member.dto.MemberLoginRequest;
import com.study.springStudy.domain.member.dto.MemberLoginResponse;
import com.study.springStudy.domain.member.service.MemberService;
import com.study.springStudy.global.component.jwt.security.MemberLoginActive;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final MemberService memberService;
    private final RedisTemplate<String, String> redisTemplate;

    @GetMapping("/login")
    public String loginPage() {
        return "login"; // JSP 파일 경로: /WEB-INF/views/login.jsp
    }

    @GetMapping("/main")
    public String mainPage(@ModelAttribute("csrfToken") String csrfToken, Model model) {
        model.addAttribute("csrfToken", csrfToken);
        System.out.println("csrfToken: " + csrfToken);

        return "main"; // JSP 파일 경로: /WEB-INF/views/main.jsp
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

            // 메인 페이지로 리다이렉트
            return "redirect:/main";
        } catch (Exception e) {
            // 로그인 실패 시 에러 메시지 전달
            redirectAttributes.addFlashAttribute("error", "Invalid username or password");
            return "redirect:/login";
        }
    }



    @PostMapping("/logout")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    public ResponseEntity<?> performSecureAction(@AuthenticationPrincipal MemberLoginActive loginActive,
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
        return ResponseEntity.ok(Map.of("redirectUrl", "/login", "message", "Logout successful"));
    }
}
