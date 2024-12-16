package com.msa.member_service.domain.member.controller;

import com.msa.member_service.domain.member.dto.*;
import com.msa.member_service.domain.member.entity.enums.MemberRole;
import com.msa.member_service.domain.member.exception.JwtTokenErrorCode;
import com.msa.member_service.domain.member.exception.JwtTokenException;
import com.msa.member_service.domain.member.service.MemberService;
import com.msa.member_service.global.common.dto.Message;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;
    private final JwtTokenPropsInfo jwtTokenPropsInfo;

    @PostMapping("/logout")
    public ResponseEntity<?> logoutMember(HttpServletRequest request,
                                                      HttpServletResponse response) {
        String accessToken = getAccessToken(request);
        MemberLoginActive memberLoginActive = parseAccessToken(accessToken);
        if (!hasAnyRole(memberLoginActive, "ADMIN") && !hasAnyRole(memberLoginActive, "USER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Message.success());
        }
        String res = memberService.logoutMember(memberLoginActive.email());
        // 쿠키 삭제
        Cookie accessTokenCookie = new Cookie("accessToken", null);
        accessTokenCookie.setMaxAge(0);
        accessTokenCookie.setPath("/");
        response.addCookie(accessTokenCookie);
        return ResponseEntity.ok().body(Message.success(res));
    }


    @GetMapping("/get")
    public ResponseEntity<?> getMember(HttpServletRequest request) {
        String accessToken = getAccessToken(request);
        MemberLoginActive memberLoginActive = parseAccessToken(accessToken);
        if (!hasAnyRole(memberLoginActive, "ADMIN") && !hasAnyRole(memberLoginActive, "USER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Message.success());
        }
        MemberInfo info = memberService.getMember(memberLoginActive.email());
        return ResponseEntity.ok().body(Message.success(info));
    }


    @DeleteMapping("/delete")
    public ResponseEntity<Message<Void>> deleteMember(HttpServletRequest request, HttpServletResponse response) {
        String accessToken = getAccessToken(request);
        MemberLoginActive memberLoginActive = parseAccessToken(accessToken);
        if (!hasAnyRole(memberLoginActive, "ADMIN") && !hasAnyRole(memberLoginActive, "USER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Message.success());
        }
        String email = memberLoginActive.email();
        memberService.deleteMember(email);
        // 쿠키 삭제
        Cookie accessTokenCookie = new Cookie("accessToken", null);
        accessTokenCookie.setMaxAge(0);
        accessTokenCookie.setPath("/");
        response.addCookie(accessTokenCookie);
        return ResponseEntity.ok().body(Message.success());
    }

    @PatchMapping("/update")
    public ResponseEntity<Message<Void>> updateImageAndNicknameMember(HttpServletRequest request,
                                                                      @RequestBody MemberUpdateRequest updateRequest) {
        String accessToken = getAccessToken(request);
        MemberLoginActive memberLoginActive = parseAccessToken(accessToken);
        if (!hasAnyRole(memberLoginActive, "ADMIN") && !hasAnyRole(memberLoginActive, "USER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Message.success());
        }
        memberService.updateProfileImageAndNickNameMember(memberLoginActive.id(), updateRequest);
        return ResponseEntity.ok().body(Message.success());
    }

    @PatchMapping("/password/change")
    public ResponseEntity<Message<Void>> updatePasswordMember(HttpServletRequest request,
                                                              @Valid @RequestBody MemberPasswordChangeRequest passwordChangeRequest) {
        String accessToken = getAccessToken(request);
        MemberLoginActive memberLoginActive = parseAccessToken(accessToken);
        if (!hasAnyRole(memberLoginActive, "ADMIN") && !hasAnyRole(memberLoginActive, "USER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Message.success());
        }
        memberService.updatePasswordMember(memberLoginActive.id(), passwordChangeRequest);
        return ResponseEntity.ok().body(Message.success());
    }

    @PostMapping("/reissue/{memberEmail}")
    public ResponseEntity<Message<String>> reissueAccessToken(@PathVariable String memberEmail) {
        String reissueAccessToken = memberService.reissueAccessToken(memberEmail);
        return ResponseEntity.ok().body(Message.success(reissueAccessToken));
    }

    private boolean hasAnyRole(MemberLoginActive memberLoginActive, String role) {
        try {
            return memberLoginActive.role().name().equals(role);// 모두 일치하지 않으면 false
        } catch (Exception e) {
            e.printStackTrace();
            return false; // 예외 발생 시 권한 없음
        }
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

    private MemberLoginActive parseAccessToken(String accessToken) {
        // 액세스 토큰 발급
        Claims payload = parseToken(accessToken, jwtTokenPropsInfo.accessKey());

        // 파싱된 데이터를 기반으로 MemberLoginActive 객체 생성 및 반환
        return new MemberLoginActive(
                Long.valueOf(payload.getId()),
                payload.get("email", String.class),
                payload.get("name", String.class),
                payload.get("nickname", String.class),
                MemberRole.fromName(payload.get("role", String.class))
        );
    }

    private Claims parseToken(String token, String secretKey) {
        Claims payload;
        try {
            // 토큰을 파싱하여 payload를 반환합니다. 이 과정에서 토큰의 무결성과 유효성이 검증됩니다.
            payload = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                    .build()
                    .parseSignedClaims(token).getPayload();
        } catch (ExpiredJwtException e) {
            // 토큰 만료 예외 처리
            throw new JwtTokenException(JwtTokenErrorCode.EXPIRED_TOKEN);
        } catch (MalformedJwtException | SecurityException | IllegalArgumentException e) {
            // 토큰 형식 불일치 예외 처리
            throw new JwtTokenException(JwtTokenErrorCode.INVALID_TOKEN);
        }
//        } catch (SignatureException e) {
//            // 토큰 서명 검증 실패 예외 처리
//            throw new JwtTokenException(JwtTokenErrorCode.SIGNATURE_INVALID);
//        }
        return payload;
    }
}
