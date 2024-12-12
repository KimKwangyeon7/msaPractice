package com.msa.auth_service.global.component.jwt.security;


import com.msa.auth_service.domain.member.entity.enums.MemberRole;

public record MemberLoginActive(
        Long id,
        String email,
        String name,
        String nickname,
        MemberRole role
) {
}
