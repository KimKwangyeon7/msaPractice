package com.msa.auth_service.domain.member.dto;

public record MemberLoginRequest(String email, String password) {
}
