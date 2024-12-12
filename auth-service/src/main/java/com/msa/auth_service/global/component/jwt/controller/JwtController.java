package com.msa.auth_service.global.component.jwt.controller;

import com.msa.auth_service.domain.member.entity.Member;
import com.msa.auth_service.global.component.jwt.service.JwtTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class JwtController {
    private final JwtTokenService jwtTokenService;

    @PostMapping("/reissue/accessToken")
    public ResponseEntity<String> reissueAccessToken(@RequestBody Member member) {
        // 새로운 Access Token을 발급
        String newAccessToken = jwtTokenService.reissueAccessToken(member);
        return ResponseEntity.ok(newAccessToken);
    }
}
