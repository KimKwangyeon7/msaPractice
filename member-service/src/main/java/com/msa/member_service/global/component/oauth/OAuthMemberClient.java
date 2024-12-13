package com.msa.member_service.global.component.oauth;


import com.msa.member_service.domain.member.entity.Member;
import com.msa.member_service.global.component.oauth.vendor.enums.OAuthDomain;

public interface OAuthMemberClient {
    OAuthDomain getOAuthDomain();

    Member fetch(OAuthDomain oAuthDomain, String authCode);
}
