package com.msa.alarm_service.domain.member.dto;


import com.msa.alarm_service.domain.member.entity.enums.MemberRole;
import com.msa.alarm_service.domain.member.entity.enums.OAuthDomain;

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
