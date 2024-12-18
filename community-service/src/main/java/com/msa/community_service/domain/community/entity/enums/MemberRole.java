package com.msa.community_service.domain.community.entity.enums;

public enum MemberRole {
    USER, ADMIN;

    public static MemberRole fromName(String roleName) {
        return MemberRole.valueOf(roleName.toUpperCase());
    }
}
