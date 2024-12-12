package com.msa.auth_service.global.component.jwt.service;


import com.msa.auth_service.domain.member.dto.MemberInfo;
import com.msa.auth_service.domain.member.dto.MemberLoginResponse;
import com.msa.auth_service.domain.member.entity.Member;
import com.msa.auth_service.global.component.jwt.JwtTokenProvider;
import com.msa.auth_service.global.component.jwt.dto.JwtTokenInfo;
import com.msa.auth_service.global.component.jwt.repository.RefreshTokenRepository;
import com.msa.auth_service.global.exception.GlobalErrorCode;
import com.msa.auth_service.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtTokenServiceImpl implements JwtTokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;


    @Override
    public MemberLoginResponse issueAndSaveJwtToken(Member member) {
        System.out.println("jwt 서비스임플 안!");
        String accessToken = jwtTokenProvider.issueAccessToken(member);
        String refreshToken = jwtTokenProvider.issueRefreshToken();

        log.info("== {} 회원에 대한 토큰 발급: {}", member.getEmail(), accessToken);
        System.out.println(member.getEmail() + " 회원에 대한 토큰 발급 " + accessToken);
        try {

            refreshTokenRepository.save(member.getEmail(), refreshToken);
            System.out.println("레디스 저장 성공!");
        } catch (Exception e) {
            System.out.println("레디스 저장 실패!");
            throw new GlobalException(GlobalErrorCode.REDIS_CONNECTION_FAILURE);
        }

        JwtTokenInfo tokenInfo = new JwtTokenInfo(accessToken);

        MemberInfo memberInfo = new MemberInfo(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getNickname(),
                member.getProfileImage(),
                member.getRole(),
                member.getOAuthDomain()
        );

        return new MemberLoginResponse(tokenInfo, memberInfo);
    }

    @Override
    public String reissueAccessToken(Member member) {
        String refreshToken = refreshTokenRepository.find(member.getEmail())
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.REDIS_NOT_TOKEN));

        return jwtTokenProvider.issueAccessToken(member);
    }
}
