package com.msa.member_service.global.component.oauth;


import com.msa.member_service.global.component.oauth.vendor.enums.OAuthDomain;

public interface OAuthCodeUrlProvider {
    OAuthDomain support();

    String provide(OAuthDomain oAuthDomain);
}

