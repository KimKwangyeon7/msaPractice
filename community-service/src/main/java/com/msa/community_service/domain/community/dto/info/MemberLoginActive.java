package com.msa.community_service.domain.community.dto.info;

import com.msa.community_service.domain.community.entity.enums.MemberRole;

public record MemberLoginActive(
        Long id,
        String email,
        String name,
        String nickname,
        MemberRole role
) {
}

