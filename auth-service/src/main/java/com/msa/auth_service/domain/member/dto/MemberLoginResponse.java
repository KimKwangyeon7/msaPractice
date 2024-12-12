package com.msa.auth_service.domain.member.dto;


import com.msa.auth_service.global.component.jwt.dto.JwtTokenInfo;

public record MemberLoginResponse(JwtTokenInfo tokenInfo, MemberInfo memberInfo) {
}
