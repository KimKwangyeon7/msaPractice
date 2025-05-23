package com.msa.auth_service.domain.member.service;

import com.msa.auth_service.domain.member.dto.MemberLoginResponse;
import com.msa.auth_service.domain.member.entity.Member;
import com.msa.auth_service.domain.member.exception.MemberErrorCode;
import com.msa.auth_service.domain.member.exception.MemberException;
import com.msa.auth_service.domain.member.repository.MemberRepository;
import com.msa.auth_service.global.component.csrf.repository.CustomCsrfTokenRepository;
import com.msa.auth_service.global.component.jwt.service.JwtTokenService;
import com.msa.auth_service.global.component.oauth.OAuthCodeUrlProvider;
import com.msa.auth_service.global.component.oauth.OAuthMemberClient;
import com.msa.auth_service.global.component.oauth.vendor.enums.OAuthDomain;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class OAuthServiceImpl implements OAuthService {

    private final OAuthCodeUrlProvider oAuthCodeUrlProvider;
    private final OAuthMemberClient oAuthMemberClient;
    private final MemberRepository memberRepository;
    private final JwtTokenService jwtTokenService;
    private final CustomCsrfTokenRepository customCsrfTokenRepository;

    @Transactional(readOnly = true)
    @Override
    public String provideAuthCodeRequestUrlOAuth(OAuthDomain oAuthDomain) {
        return oAuthCodeUrlProvider.provide(oAuthDomain);
    }

    @Override
    public MemberLoginResponse loginOAuth(OAuthDomain oAuthDomain, String authCode, HttpServletResponse response) {
        Member oauthMember = oAuthMemberClient.fetch(oAuthDomain, authCode);
        Optional<Member> existingMemberOpt = memberRepository.findByEmail(oauthMember.getEmail());

        // csrf 토큰
        customCsrfTokenRepository.issueAndSaveCsrfToken(oauthMember.getEmail(), response);

        if (existingMemberOpt.isPresent()) {
            Member existingMember = existingMemberOpt.get();
            if (!existingMember.getOAuthDomain().equals(oAuthDomain)) {
                throw new MemberException(MemberErrorCode.EXIST_MEMBER_EMAIL);
            }
            return jwtTokenService.issueAndSaveJwtToken(existingMember);
        } else {
            Member member = memberRepository.save(oauthMember);
            return jwtTokenService.issueAndSaveJwtToken(member);
        }
    }
}
