package com.msa.auth_service.global.component.oauth;


import com.msa.auth_service.global.component.oauth.vendor.enums.OAuthDomain;

public interface OAuthCodeUrlProvider {
    OAuthDomain support();

    String provide(OAuthDomain oAuthDomain);
}

