package com.msa.auth_service.domain.member.service;


import com.msa.auth_service.domain.member.dto.MemberLoginResponse;
import com.msa.auth_service.global.component.oauth.vendor.enums.OAuthDomain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface OAuthService {
    String provideAuthCodeRequestUrlOAuth(OAuthDomain oAuthDomain);

    MemberLoginResponse loginOAuth(OAuthDomain oAuthDomain, String authCode, HttpServletResponse response);
}
