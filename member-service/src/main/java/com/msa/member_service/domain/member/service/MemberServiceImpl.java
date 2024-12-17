package com.msa.member_service.domain.member.service;

import com.msa.member_service.domain.member.dto.MemberInfo;
import com.msa.member_service.domain.member.dto.MemberPasswordChangeRequest;
import com.msa.member_service.domain.member.dto.MemberUpdateRequest;
import com.msa.member_service.domain.member.entity.Member;
import com.msa.member_service.domain.member.entity.enums.MemberRole;
import com.msa.member_service.domain.member.exception.MemberErrorCode;
import com.msa.member_service.domain.member.exception.MemberException;
import com.msa.member_service.domain.member.repository.MemberRepository;
import com.msa.member_service.global.component.oauth.vendor.enums.OAuthDomain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final WebClient authServiceWebClient;
    private final RedisTemplate<String, String> redisTemplate;
    private static final Pattern MEMBER_INFO_PATTERN = Pattern.compile(
            "MemberInfo\\[id=(\\d+), email=([^,]+), name=(.*?), nickname=(.*?), profileImage=(.*?), role=(.*?), provider=(.*?)]"
    );

    @Override
    public String logoutMember(String memberEmail) {
        return authServiceWebClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/auth/member/logout/{email}")
                        .build(memberEmail)) // PathVariable로 이메일을 설정
                .retrieve()
                .bodyToMono(String.class)
                .block(); // 동기 처리, 필요에 따라 비동기로 변경 가능
    }

    @Override
    @Transactional(readOnly = true)
    public MemberInfo getMember(String email) {
        String value = redisTemplate.opsForValue().get("memberInfo::" + email);
        if (value == null) {
            throw new IllegalArgumentException("사용자 정보를 찾을 수 없습니다: " + email);
        }
        return parseMemberInfo(value);
    }

    public MemberInfo parseMemberInfo(String rawData) {
        Matcher matcher = MEMBER_INFO_PATTERN.matcher(rawData);
        if (matcher.matches()) {
            Long id = Long.parseLong(matcher.group(1));
            String email = matcher.group(2);
            String name = matcher.group(3);
            String nickname = matcher.group(4);
            String profileImage = "null".equals(matcher.group(5)) ? null : matcher.group(5);
            MemberRole role = MemberRole.valueOf(matcher.group(6));
            OAuthDomain provider = "null".equals(matcher.group(7)) ? null : OAuthDomain.valueOf(matcher.group(7));

            // 새로운 MemberInfo 객체 생성
            return new MemberInfo(id, email, name, nickname, profileImage, role, provider);
        } else {
            throw new IllegalArgumentException("Invalid MemberInfo format: " + rawData);
        }
    }

    @Override
    public void deleteMember(String memberEmail) {
//        // MongoDB에 저장된 데이터 삭제
//        simulationRepository.deleteByMemberId(memberId);
//        recommendationRepository.deleteByUserId(memberId);
//        commercialAnalysisRepository.deleteByMemberId(memberId);
        // JPA에 저장된 데이터 삭제
        //Member member = findMemberById(memberId);
        authServiceWebClient.delete()
                .uri(uriBuilder -> uriBuilder
                        .path("/auth/member/delete/{email}")
                        .build(memberEmail)) // PathVariable로 이메일을 설정
                .retrieve()
                .bodyToMono(String.class)
                .block(); // 동기 처리, 필요에 따라 비동기로 변경 가능
    }

    @Override
    public void updateProfileImageAndNickNameMember(Long memberId, MemberUpdateRequest updateRequest) {
        authServiceWebClient.patch()
                .uri(uriBuilder -> uriBuilder
                        .path("/auth/member/update/{memberId}")
                        .build(memberId)) // PathVariable 설정
                .bodyValue(updateRequest) // RequestBody 설정
                .retrieve()
                .bodyToMono(String.class)
                .block(); // 동기 처리
    }

    @Override
    public void updatePasswordMember(Long memberId, MemberPasswordChangeRequest passwordChangeRequest) {
        authServiceWebClient.patch()
                .uri(uriBuilder -> uriBuilder
                        .path("/auth/member/password/change/{memberId}")
                        .build(memberId)) // PathVariable 설정
                .bodyValue(passwordChangeRequest) // RequestBody 설정
                .retrieve()
                .bodyToMono(String.class)
                .block(); // 동기 처리
    }

    // 인증서버로 통신
    @Override
    public String reissueAccessToken(String memberEmail) {
        return authServiceWebClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/auth/member/reissue/{email}")
                        .build(memberEmail)) // PathVariable로 이메일을 설정
                .retrieve()
                .bodyToMono(String.class)
                .block(); // 동기 처리, 필요에 따라 비동기로 변경 가능
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
