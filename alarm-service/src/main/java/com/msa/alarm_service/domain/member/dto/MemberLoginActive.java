package com.msa.alarm_service.domain.member.dto;


import com.msa.alarm_service.domain.member.entity.enums.MemberRole;

public record MemberLoginActive(
        Long id,
        String email,
        String name,
        String nickname,
        MemberRole role
) {
}

