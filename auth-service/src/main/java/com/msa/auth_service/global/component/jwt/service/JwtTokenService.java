package com.msa.auth_service.global.component.jwt.service;


import com.msa.auth_service.domain.member.dto.MemberLoginResponse;
import com.msa.auth_service.domain.member.entity.Member;

public interface JwtTokenService {
    MemberLoginResponse issueAndSaveJwtToken(Member member);

    String reissueAccessToken(Member member);
}
