package com.msa.member_service.domain.member.controller;


import com.msa.member_service.domain.member.service.MemberService;
import com.msa.member_service.global.common.dto.Message;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/logout/{email}")
    //@PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    public ResponseEntity<Message<String>> logoutMember(@PathVariable String email, HttpServletRequest request,
                                                      HttpServletResponse response) {
        String csrfToken = request.getHeader("X-CSRF-TOKEN");
        String accessToken = "";
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) { // JWT 쿠키 이름 확인
                    accessToken =  cookie.getValue();
                }
            }
        }
        System.out.println("csrf 토큰: " + csrfToken + "jwt 토큰: " + accessToken);
        String res = memberService.logoutMember(email);
        // 쿠키 삭제
        Cookie accessTokenCookie = new Cookie("accessToken", null);
        accessTokenCookie.setMaxAge(0);
        accessTokenCookie.setPath("/");
        response.addCookie(accessTokenCookie);
        return ResponseEntity.ok().body(Message.success(res));
    }


//    @GetMapping("/get")
//    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
//    public ResponseEntity<Message<MemberInfo>> getMember(@AuthenticationPrincipal MemberLoginActive loginActive) {
//        MemberInfo info = memberService.getMember(loginActive.id());
//        return ResponseEntity.ok().body(Message.success(info));
//    }
//
//
//    @DeleteMapping("/delete")
//    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
//    public ResponseEntity<Message<Void>> deleteMember(@AuthenticationPrincipal MemberLoginActive loginActive) {
//        memberService.deleteMember(loginActive.id());
//        return ResponseEntity.ok().body(Message.success());
//    }
//
//    @PatchMapping("/update")
//    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
//    public ResponseEntity<Message<Void>> updateImageAndNicknameMember(@AuthenticationPrincipal MemberLoginActive loginActive,
//                                                                      @RequestBody MemberUpdateRequest updateRequest) {
//        memberService.updateProfileImageAndNickNameMember(loginActive.id(), updateRequest);
//        return ResponseEntity.ok().body(Message.success());
//    }
//
//
//    @PatchMapping("/password/change")
//    @PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
//    public ResponseEntity<Message<Void>> updatePasswordMember(@AuthenticationPrincipal MemberLoginActive loginActive,
//                                                              @Valid @RequestBody MemberPasswordChangeRequest passwordChangeRequest) {
//        memberService.updatePasswordMember(loginActive.id(), passwordChangeRequest);
//        return ResponseEntity.ok().body(Message.success());
//    }

    @PostMapping("/reissue/{memberEmail}")
    public ResponseEntity<Message<String>> reissueAccessToken(@PathVariable String memberEmail) {
        String reissueAccessToken = memberService.reissueAccessToken(memberEmail);
        return ResponseEntity.ok().body(Message.success(reissueAccessToken));
    }
}
