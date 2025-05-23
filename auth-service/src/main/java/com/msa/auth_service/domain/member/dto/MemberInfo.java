package com.msa.auth_service.domain.member.dto;


import com.msa.auth_service.domain.member.entity.enums.MemberRole;
import com.msa.auth_service.global.component.oauth.vendor.enums.OAuthDomain;

public record MemberInfo(
        Long id,
        String email,
        String name,
        String nickname,
        String profileImage,
        MemberRole role,
        OAuthDomain provider
) {
}
