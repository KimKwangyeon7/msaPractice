package com.msa.member_service.domain.member.controller;

import com.study.springStudy.domain.member.dto.*;
import com.study.springStudy.domain.member.service.MemberService;
import com.study.springStudy.global.common.dto.Message;
import com.study.springStudy.global.component.jwt.security.MemberLoginActive;
import com.study.springStudy.global.component.jwt.service.JwtTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/member")
public class MemberController {

    private final MemberService memberService;


    @PostMapping("/signup")
    public ResponseEntity<Message<Void>> signupMember(@Valid @RequestBody MemberSignupRequest signupRequest) {
        memberService.signupMember(signupRequest);
        return ResponseEntity.ok().body(Message.success());
    }

    @PostMapping("/login")
    public ResponseEntity<Message<MemberLoginResponse>> loginMember(@RequestBody MemberLoginRequest loginRequest,
                                                                    HttpServletResponse response) {
        MemberLoginResponse loginResponse = memberService.loginMember(loginRequest, response);
        // JWT 토큰을 쿠키에 저장
//        Cookie accessTokenCookie = new Cookie("accessToken", loginResponse.tokenInfo().accessToken());
//        accessTokenCookie.setPath("/");
//        accessTokenCookie.setMaxAge(25200); // 4200분(25200초)으로 설정 (25200)
//        response.addCookie(accessTokenCookie);
        return ResponseEntity.ok().body(Message.success(loginResponse));
    }


    @PostMapping("/logout")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    public ResponseEntity<Message<Void>> logoutMember(@AuthenticationPrincipal MemberLoginActive loginActive,
                                                      HttpServletResponse response) {
        memberService.logoutMember(loginActive.email());;
        // 쿠키 삭제
        Cookie accessTokenCookie = new Cookie("accessToken", null);
        accessTokenCookie.setMaxAge(0);
        accessTokenCookie.setPath("/");
        response.addCookie(accessTokenCookie);
        return ResponseEntity.ok().body(Message.success());
    }


    @GetMapping("/get")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    public ResponseEntity<Message<MemberInfo>> getMember(@AuthenticationPrincipal MemberLoginActive loginActive) {
        MemberInfo info = memberService.getMember(loginActive.id());
        return ResponseEntity.ok().body(Message.success(info));
    }


    @DeleteMapping("/delete")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    public ResponseEntity<Message<Void>> deleteMember(@AuthenticationPrincipal MemberLoginActive loginActive) {
        memberService.deleteMember(loginActive.id());
        return ResponseEntity.ok().body(Message.success());
    }

    @PatchMapping("/update")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    public ResponseEntity<Message<Void>> updateImageAndNicknameMember(@AuthenticationPrincipal MemberLoginActive loginActive,
                                                                      @RequestBody MemberUpdateRequest updateRequest) {
        memberService.updateProfileImageAndNickNameMember(loginActive.id(), updateRequest);
        return ResponseEntity.ok().body(Message.success());
    }


    @PatchMapping("/password/change")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    public ResponseEntity<Message<Void>> updatePasswordMember(@AuthenticationPrincipal MemberLoginActive loginActive,
                                                              @Valid @RequestBody MemberPasswordChangeRequest passwordChangeRequest) {
        memberService.updatePasswordMember(loginActive.id(), passwordChangeRequest);
        return ResponseEntity.ok().body(Message.success());
    }

    @PostMapping("/reissue/accessToken/{memberEmail}")
    public ResponseEntity<Message<String>> reissueAccessToken(@PathVariable String memberEmail) {
        String reissueAccessToken = memberService.reissueAccessToken(memberEmail);
        return ResponseEntity.ok().body(Message.success(reissueAccessToken));
    }
}
