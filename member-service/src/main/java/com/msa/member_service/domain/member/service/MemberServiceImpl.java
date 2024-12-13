package com.msa.member_service.domain.member.service;

import com.msa.member_service.domain.member.dto.MemberInfo;
import com.msa.member_service.domain.member.entity.Member;
import com.msa.member_service.domain.member.exception.MemberErrorCode;
import com.msa.member_service.domain.member.exception.MemberException;
import com.msa.member_service.domain.member.repository.MemberRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final WebClient authServiceWebClient;

//    private final SimulationRepository simulationRepository;
//    private final RecommendationRepository recommendationRepository;
//    private final CommercialAnalysisRepository commercialAnalysisRepository;

    @Override
    public String logoutMember(String memberEmail) {
        return authServiceWebClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/auth/logout/{email}")
                        .build(memberEmail)) // PathVariable로 이메일을 설정
                .retrieve()
                .bodyToMono(String.class)
                .block(); // 동기 처리, 필요에 따라 비동기로 변경 가능
    }

//    @Override
//    @Transactional(readOnly = true)
//    public MemberInfo getMember(Long memberId) {
//        Member member = findMemberById(memberId);
//
//        return new MemberInfo(
//                member.getId(),
//                member.getEmail(),
//                member.getName(),
//                member.getNickname(),
//                member.getProfileImage(),
//                member.getRole(),
//                member.getOAuthDomain()
//        );
//    }
//
//    @Override
//    public void deleteMember(Long memberId) {
////        // MongoDB에 저장된 데이터 삭제
////        simulationRepository.deleteByMemberId(memberId);
////        recommendationRepository.deleteByUserId(memberId);
////        commercialAnalysisRepository.deleteByMemberId(memberId);
//
//        // JPA에 저장된 데이터 삭제
//        Member member = findMemberById(memberId);
//        refreshTokenRepository.delete(member.getEmail());
//        customCsrfTokenRepository.delete(member.getEmail());
//        memberRepository.deleteById(memberId);
//    }
//
//    @Override
//    public void updateProfileImageAndNickNameMember(Long memberId, MemberUpdateRequest updateRequest) {
//        Member member = findMemberById(memberId);
//
//        member.updateProfileImageAndNickname(updateRequest);
//    }
//
//    @Override
//    public void updatePasswordMember(Long memberId, MemberPasswordChangeRequest passwordChangeRequest) {
//        Member member = findMemberById(memberId);
//
//        String realPassword = member.getPassword();
//
//        // 현재 비밀번호 제대로 입력했는지 확인
//        if (!passwordEncoder.matches(passwordChangeRequest.nowPassword(), realPassword)) {
//            throw new MemberException(MemberErrorCode.NOT_MATCH_PASSWORD);
//        }
//
//        // 현재 비밀번호와 변경하려는 비밀번호가 같은지 확인 (같은 경우 Exception 발생)
//        if (passwordEncoder.matches(passwordChangeRequest.changePassword(), realPassword)) {
//            throw new MemberException(MemberErrorCode.CURRENT_CHANGE_MATCH_PASSWORD);
//        }
//
//        // 비밀번호 변경과 비밀번호 변경 확인 서로 같은지 확인 (다른 경우 Exception 발생)
//        if (!passwordChangeRequest.changePassword().equals(passwordChangeRequest.changePasswordCheck())) {
//            throw new MemberException(MemberErrorCode.PASSWORD_CONFIRMATION_MISMATCH);
//        }
//
//        member.updatePassword(passwordEncoder.encode(passwordChangeRequest.changePassword()));
//    }

    // 인증서버로 통신
    @Override
    public String reissueAccessToken(String memberEmail) {
        return authServiceWebClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/auth/reissue/{email}")
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
