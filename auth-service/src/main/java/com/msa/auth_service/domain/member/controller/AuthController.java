package com.msa.auth_service.domain.member.controller;


import com.msa.auth_service.domain.member.dto.*;
import com.msa.auth_service.domain.member.entity.Member;
import com.msa.auth_service.domain.member.exception.MemberErrorCode;
import com.msa.auth_service.domain.member.exception.MemberException;
import com.msa.auth_service.domain.member.repository.MemberRepository;
import com.msa.auth_service.domain.member.service.MemberService;
import com.msa.auth_service.global.common.dto.Message;
import com.msa.auth_service.global.component.jwt.JwtTokenProvider;
import com.msa.auth_service.global.component.jwt.repository.RefreshTokenRepository;
import com.msa.auth_service.global.component.jwt.security.MemberLoginActive;
import com.msa.auth_service.global.component.jwt.service.JwtTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final MemberService memberService;
    private final RedisTemplate<String, String> redisTemplate;
    private final JwtTokenService jwtTokenService;
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @PostMapping("/validate")
    //@PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
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
    @ResponseBody
    public ResponseEntity<?> login(@RequestParam String username,
                                   @RequestParam String password,
                                   HttpServletRequest request,
                                   HttpServletResponse response) {
        try {
            // 로그인 요청 처리
            MemberLoginRequest loginRequest = new MemberLoginRequest(username, password);
            MemberLoginResponse memberLoginResponse = memberService.loginMember(loginRequest, response);

            // CSRF 토큰과 이메일을 세션에 저장
            String csrfToken = response.getHeader("X-CSRF-TOKEN");
            String email = memberLoginResponse.memberInfo().email();
            HttpSession session = request.getSession(true);
            session.setAttribute("csrfToken", csrfToken);
            session.setAttribute("email", email);

            return ResponseEntity.ok(Map.of(
                    "message", "Login successful"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error", "Invalid username or password"
            ));
        }
    }

    @GetMapping("/main")
    public String mainPage(HttpSession session, Model model, HttpServletRequest request, HttpServletResponse response) {
        // 세션에서 CSRF 토큰과 이메일 가져오기
        String csrfToken = (String) session.getAttribute("csrfToken");
        String email = (String) session.getAttribute("email");

        // 세션에 저장된 값이 없을 경우 처리
        if (csrfToken == null || email == null) {
            model.addAttribute("error", "Session expired. Please log in again.");
            return "redirect:/auth/login";
        }

        String accessToken = getAccessToken(request);
        if (accessToken == null) {
            String refreshToken = refreshTokenRepository.find(email).orElse(null);
            if (refreshToken == null) {
                model.addAttribute("error", "Session expired. Please log in again.");
                return "redirect:/auth/login";
            }
            accessToken = jwtTokenProvider.issueAccessToken(findMemberByEmail(email));
            Cookie accessTokenCookie = new Cookie("accessToken", accessToken);
            accessTokenCookie.setPath("/"); // 쿠키의 경로 설정
            accessTokenCookie.setMaxAge(6000); // 6000초

            // HttpOnly 설정: JavaScript에서 쿠키에 접근하지 못하도록 제한
            accessTokenCookie.setHttpOnly(true);
            // Secure 설정: HTTPS에서만 전송되도록 제한 (HTTPS를 사용하지 않는 경우 주석 처리 가능)
            //accessTokenCookie.setSecure(false);
            // SameSite 설정: 요청의 출처를 제한하여 CSRF 공격을 방지
            // SameSite 설정은 Java 11 이상에서 지원됩니다.
            //accessTokenCookie.setAttribute("SameSite", "None");
            response.addCookie(accessTokenCookie);
        }

        model.addAttribute("csrfToken", csrfToken);
        model.addAttribute("email", email);

        return "main"; // JSP 파일 경로: /WEB-INF/views/main.jsp
    }

    @GetMapping("/member/password/change")
    public String changePwdPage(HttpSession session, Model model) {
        // 세션에서 CSRF 토큰과 이메일 가져오기
        String csrfToken = (String) session.getAttribute("csrfToken");
        String email = (String) session.getAttribute("email");

        // 세션에 저장된 값이 없을 경우 처리
        if (csrfToken == null || email == null) {
            model.addAttribute("error", "Session expired. Please log in again.");
            return "redirect:/auth/login";
        }

        model.addAttribute("csrfToken", csrfToken);
        model.addAttribute("email", email);

        return "passwordChange"; // JSP 파일 경로: /WEB-INF/views/main.jsp
    }

    @GetMapping("/member/update")
    public String updateProfilePage(HttpSession session, Model model) {
        // 세션에서 CSRF 토큰과 이메일 가져오기
        String csrfToken = (String) session.getAttribute("csrfToken");
        String email = (String) session.getAttribute("email");

        // 세션에 저장된 값이 없을 경우 처리
        if (csrfToken == null || email == null) {
            model.addAttribute("error", "Session expired. Please log in again.");
            return "redirect:/auth/login";
        }
        Member member = findMemberByEmail(email);

        model.addAttribute("profile", member.getProfileImage());
        model.addAttribute("nickname", member.getNickname());
        model.addAttribute("csrfToken", csrfToken);
        model.addAttribute("email", email);

        return "profileUpdate"; // JSP 파일 경로: /WEB-INF/views/main.jsp
    }


    @PostMapping("/member/logout/{email}")
    public ResponseEntity<?> logout(@PathVariable String email,
                                    HttpServletResponse response, HttpSession session) {
        try {
            // 로그아웃 처리
            memberService.logoutMember(email);
            // AccessToken 쿠키 초기화
            Cookie accessTokenCookie = new Cookie("accessToken", null);
            accessTokenCookie.setMaxAge(0);
            accessTokenCookie.setPath("/");
            response.addCookie(accessTokenCookie);
            // SecurityContext 및 세션 초기화
            SecurityContextHolder.clearContext();
            session.invalidate();
            // 성공 응답 메시지
            return ResponseEntity.ok(Message.success("Logout successful"));
        } catch (Exception e) {
            // 에러 응답 메시지
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Message.success("Logout failed"));
        }
    }

    /**
     * PathVariable로 이메일을 받아 Access Token 재발급
     *
     * @param email 이메일 주소
     * @return 새롭게 발급된 Access Token
     */
    @PostMapping("/member/reissue/{email}")
    public ResponseEntity<?> reissueAccessToken(@PathVariable("email") String email, HttpServletResponse response) {
        try {
            // 이메일로 멤버 검증
            Member member = findMemberByEmail(email);
            // Access Token 생성
            String newAccessToken = jwtTokenService.reissueAccessToken(member);
            // 응답 헤더 쿠키에 추가
            Cookie accessTokenCookie = new Cookie("accessToken", newAccessToken);
            accessTokenCookie.setPath("/"); // 쿠키의 경로 설정
            accessTokenCookie.setMaxAge(6000); // 6000초
            // HttpOnly 설정: JavaScript에서 쿠키에 접근하지 못하도록 제한
            accessTokenCookie.setHttpOnly(true);
            // Secure 설정: HTTPS에서만 전송되도록 제한 (HTTPS를 사용하지 않는 경우 주석 처리 가능)
            //accessTokenCookie.setSecure(true);
            // SameSite 설정: 요청의 출처를 제한하여 CSRF 공격을 방지
            // SameSite 설정은 Java 11 이상에서 지원됩니다.
            //accessTokenCookie.setAttribute("SameSite", "None");
            response.addCookie(accessTokenCookie);
            // 응답 반환
            return ResponseEntity.ok(newAccessToken);
        } catch (MemberException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Member not found: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to reissue access token");
        }
    }

    @DeleteMapping("/member/delete/{email}")
    public ResponseEntity<?> deleteMember(@PathVariable String email, HttpServletResponse response, HttpSession session) {
        try {
            // 회원 탈퇴 처리
            memberService.deleteMember(email);
            // AccessToken 쿠키 초기화
            Cookie accessTokenCookie = new Cookie("accessToken", null);
            accessTokenCookie.setMaxAge(0);
            accessTokenCookie.setPath("/");
            response.addCookie(accessTokenCookie);
            // SecurityContext 및 세션 초기화
            SecurityContextHolder.clearContext();
            session.invalidate();
            // 성공 응답 메시지
            return ResponseEntity.ok(Message.success("Logout successful"));
        } catch (Exception e) {
            // 에러 응답 메시지
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Message.success("Delete failed"));
        }
    }

    @PatchMapping("/member/update/{memberId}")
    public ResponseEntity<?> updateImageAndNicknameMember(@PathVariable Long memberId, @RequestBody MemberUpdateRequest memberUpdateRequest) {
        try {
            // 회원 정보 업데이트
            memberService.updateProfileImageAndNickNameMember(memberId, memberUpdateRequest);
            // 성공 응답
            return ResponseEntity.ok(Map.of(
                    "redirectUrl", "/auth/main",
                    "message", "Update successful"
            ));
        } catch (Exception e) {
            // 에러 처리
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "Update failed"
            ));
        }
    }

    @PatchMapping("/member/password/change/{memberId}")
    public ResponseEntity<Message<Void>> updatePasswordMember(@PathVariable Long memberId, @RequestBody MemberPasswordChangeRequest passwordChangeRequest) {
        memberService.updatePasswordMember(memberId, passwordChangeRequest);
        return ResponseEntity.ok().body(Message.success());
    }

    private Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER));
    }

    private String getAccessToken(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) { // JWT 쿠키 이름 확인
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    @PostMapping("/member/community")
    public ResponseEntity<List<MemberInfoResponse>> getMembersByIds(@RequestBody List<Long> writerIds) {
        System.out.println(writerIds.size());
        for (Long id : writerIds) {
            System.out.println("memberId: "+ id);
        }
        if (writerIds == null || writerIds.isEmpty()) {
            return ResponseEntity.badRequest().build(); // 요청된 ID가 없으면 400 Bad Request 반환
        }
        List<MemberInfoResponse> responses = memberService.findMembersByIds(writerIds);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/member/community/{writerId}")
    public ResponseEntity<MemberInfoResponse> getMemberInfoById(@PathVariable Long writerId) {
        //System.out.println("memberId: "+ writerId);
        if (writerId == null) {
            return ResponseEntity.badRequest().build(); // 요청된 ID가 없으면 400 Bad Request 반환
        }
        MemberInfoResponse response = memberService.findMemberInfoById(writerId);
        return ResponseEntity.ok(response);
    }
}
