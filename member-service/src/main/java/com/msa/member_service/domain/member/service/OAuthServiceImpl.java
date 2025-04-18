//package com.msa.member_service.domain.member.service;
//
//import com.study.springStudy.domain.member.dto.MemberLoginResponse;
//import com.study.springStudy.domain.member.entity.Member;
//import com.study.springStudy.domain.member.exception.MemberErrorCode;
//import com.study.springStudy.domain.member.exception.MemberException;
//import com.study.springStudy.domain.member.repository.MemberRepository;
//import com.study.springStudy.global.component.jwt.service.JwtTokenService;
//import com.study.springStudy.global.component.oauth.OAuthCodeUrlProvider;
//import com.study.springStudy.global.component.oauth.OAuthMemberClient;
//import com.study.springStudy.global.component.oauth.vendor.enums.OAuthDomain;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.Optional;
//
//@Slf4j
//@Service
//@Transactional
//@RequiredArgsConstructor
//public class OAuthServiceImpl implements OAuthService {
//
//    private final OAuthCodeUrlProvider oAuthCodeUrlProvider;
//    private final OAuthMemberClient oAuthMemberClient;
//    private final MemberRepository memberRepository;
//    private final JwtTokenService jwtTokenService;
//
//    @Transactional(readOnly = true)
//    @Override
//    public String provideAuthCodeRequestUrlOAuth(OAuthDomain oAuthDomain) {
//        return oAuthCodeUrlProvider.provide(oAuthDomain);
//    }
//
//    @Override
//    public MemberLoginResponse loginOAuth(OAuthDomain oAuthDomain, String authCode) {
//        Member oauthMember = oAuthMemberClient.fetch(oAuthDomain, authCode);
//        Optional<Member> existingMemberOpt = memberRepository.findByEmail(oauthMember.getEmail());
//
//
//        if (existingMemberOpt.isPresent()) {
//            Member existingMember = existingMemberOpt.get();
//            if (!existingMember.getOAuthDomain().equals(oAuthDomain)) {
//                throw new MemberException(MemberErrorCode.EXIST_MEMBER_EMAIL);
//            }
//            return jwtTokenService.issueAndSaveJwtToken(existingMember);
//        } else {
//            Member member = memberRepository.save(oauthMember);
//            return jwtTokenService.issueAndSaveJwtToken(member);
//        }
//    }
//}
