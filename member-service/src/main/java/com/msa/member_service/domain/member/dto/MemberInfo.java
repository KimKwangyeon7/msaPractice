package com.msa.member_service.domain.member.dto;


import com.study.springStudy.domain.member.entity.enums.MemberRole;
import com.study.springStudy.global.component.oauth.vendor.enums.OAuthDomain;

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
