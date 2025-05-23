package com.msa.chat_service.domain.member.entity.enums;

public enum MemberRole {
    USER, ADMIN;

    public static MemberRole fromName(String roleName) {
        return MemberRole.valueOf(roleName.toUpperCase());
    }
}
