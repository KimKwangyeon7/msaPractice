package com.msa.member_service.domain.member.dto;


import com.study.springStudy.global.component.jwt.dto.JwtTokenInfo;

public record MemberLoginResponse(JwtTokenInfo tokenInfo, MemberInfo memberInfo) {
}
