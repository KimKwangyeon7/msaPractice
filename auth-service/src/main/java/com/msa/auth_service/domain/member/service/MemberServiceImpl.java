package com.msa.auth_service.domain.member.service;

import com.msa.auth_service.domain.member.dto.*;
import com.msa.auth_service.domain.member.entity.Member;
import com.msa.auth_service.domain.member.exception.MemberErrorCode;
import com.msa.auth_service.domain.member.exception.MemberException;
import com.msa.auth_service.domain.member.repository.MemberRepository;
import com.msa.auth_service.global.component.csrf.repository.CustomCsrfTokenRepository;
import com.msa.auth_service.global.component.jwt.repository.RefreshTokenRepository;
import com.msa.auth_service.global.component.jwt.service.JwtTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final JwtTokenService jwtTokenService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CustomCsrfTokenRepository customCsrfTokenRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void signupMember(MemberSignupRequest signupRequest) {
        if (memberRepository.existsByEmail(signupRequest.getEmail())) {
            throw new MemberException(MemberErrorCode.EXIST_MEMBER_EMAIL);
        }

        signupRequest.setPassword(passwordEncoder.encode(signupRequest.getPassword()));

        memberRepository.save(signupRequest.toEntity());
    }

    @Override
    public MemberLoginResponse loginMember(MemberLoginRequest loginRequest, HttpServletResponse response) {
        //System.out.println("서비스 임플안");
        Member member = findMemberByEmail(loginRequest.email());

        String realPassword = member.getPassword();

        if (!passwordEncoder.matches(loginRequest.password(), realPassword)) {
            //System.out.println("Invalid password");
            throw new MemberException(MemberErrorCode.NOT_MATCH_PASSWORD);
        }
        // jwt 토큰
        MemberLoginResponse memberLoginResponse =  jwtTokenService.issueAndSaveJwtToken(member);
        // 응답 헤더 쿠키에 추가
        Cookie accessTokenCookie = getAccessTokenCookie(memberLoginResponse);
        response.addCookie(accessTokenCookie);
        // csrf 토큰
        customCsrfTokenRepository.issueAndSaveCsrfToken(loginRequest.email(), response);
        // memberInfo 저장
        storeUserInRedis(memberLoginResponse);
        return memberLoginResponse;
    }

    public void storeUserInRedis(MemberLoginResponse memberLoginResponse) {
        String redisKey = "memberInfo::" + memberLoginResponse.memberInfo().email();
        redisTemplate.opsForValue().set(redisKey, String.valueOf(memberLoginResponse.memberInfo()), Duration.ofHours(24));
    }

    private static Cookie getAccessTokenCookie(MemberLoginResponse memberLoginResponse) {
        Cookie accessTokenCookie = new Cookie("accessToken", memberLoginResponse.tokenInfo().accessToken());
        accessTokenCookie.setPath("/"); // 쿠키의 경로 설정
        accessTokenCookie.setMaxAge(6000); // 6000초

        // HttpOnly 설정: JavaScript에서 쿠키에 접근하지 못하도록 제한
        accessTokenCookie.setHttpOnly(true);
        // Secure 설정: HTTPS에서만 전송되도록 제한 (HTTPS를 사용하지 않는 경우 주석 처리 가능)
        accessTokenCookie.setSecure(true);
        // SameSite 설정: 요청의 출처를 제한하여 CSRF 공격을 방지
        // SameSite 설정은 Java 11 이상에서 지원됩니다.
        accessTokenCookie.setAttribute("SameSite", "None");
        return accessTokenCookie;
    }

    @Override
    public void logoutMember(String email) {
        Optional<String> token = refreshTokenRepository.find(email);

        if (token.isEmpty()) {
            throw new MemberException(MemberErrorCode.ALREADY_MEMBER_LOGOUT);
        }

        // 리프레쉬 토큰 삭제
        refreshTokenRepository.delete(email);
        customCsrfTokenRepository.delete(email);
    }

    @Override
    @Transactional(readOnly = true)
    public MemberInfo getMember(Long memberId) {
        Member member = findMemberById(memberId);

        return new MemberInfo(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getNickname(),
                member.getProfileImage(),
                member.getRole(),
                member.getOAuthDomain()
        );
    }

    @Override
    public void deleteMember(Long memberId) {
//        // MongoDB에 저장된 데이터 삭제
//        simulationRepository.deleteByMemberId(memberId);
//        recommendationRepository.deleteByUserId(memberId);
//        commercialAnalysisRepository.deleteByMemberId(memberId);

        // JPA에 저장된 데이터 삭제
        Member member = findMemberById(memberId);
        refreshTokenRepository.delete(member.getEmail());
        customCsrfTokenRepository.delete(member.getEmail());
        memberRepository.deleteById(memberId);
    }

    @Override
    public void updateProfileImageAndNickNameMember(Long memberId, MemberUpdateRequest updateRequest) {
        Member member = findMemberById(memberId);

        member.updateProfileImageAndNickname(updateRequest);
    }

    @Override
    public void updatePasswordMember(Long memberId, MemberPasswordChangeRequest passwordChangeRequest) {
        Member member = findMemberById(memberId);

        String realPassword = member.getPassword();

        // 현재 비밀번호 제대로 입력했는지 확인
        if (!passwordEncoder.matches(passwordChangeRequest.nowPassword(), realPassword)) {
            throw new MemberException(MemberErrorCode.NOT_MATCH_PASSWORD);
        }

        // 현재 비밀번호와 변경하려는 비밀번호가 같은지 확인 (같은 경우 Exception 발생)
        if (passwordEncoder.matches(passwordChangeRequest.changePassword(), realPassword)) {
            throw new MemberException(MemberErrorCode.CURRENT_CHANGE_MATCH_PASSWORD);
        }

        // 비밀번호 변경과 비밀번호 변경 확인 서로 같은지 확인 (다른 경우 Exception 발생)
        if (!passwordChangeRequest.changePassword().equals(passwordChangeRequest.changePasswordCheck())) {
            throw new MemberException(MemberErrorCode.PASSWORD_CONFIRMATION_MISMATCH);
        }

        member.updatePassword(passwordEncoder.encode(passwordChangeRequest.changePassword()));
    }

    private Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER));
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER));
    }
}
