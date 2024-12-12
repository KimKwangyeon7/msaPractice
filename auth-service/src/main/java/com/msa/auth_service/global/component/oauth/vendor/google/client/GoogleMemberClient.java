package com.msa.auth_service.global.component.oauth.vendor.google.client;


import com.msa.auth_service.domain.member.entity.Member;
import com.msa.auth_service.global.component.oauth.OAuthMemberClient;
import com.msa.auth_service.global.component.oauth.vendor.enums.OAuthDomain;
import com.msa.auth_service.global.component.oauth.vendor.google.GoogleOAuthProps;
import com.msa.auth_service.global.component.oauth.vendor.google.dto.GoogleMemberResponse;
import com.msa.auth_service.global.component.oauth.vendor.google.dto.GoogleToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Component
@RequiredArgsConstructor
public class GoogleMemberClient implements OAuthMemberClient {
    private final GoogleApiClient googleApiClient;
    private final GoogleOAuthProps props;

    @Override
    public OAuthDomain getOAuthDomain() {
        return OAuthDomain.GOOGLE;
    }

    @Override
    public Member fetch(OAuthDomain oAuthDomain, String authCode) {
        GoogleToken tokenInfo = googleApiClient.fetchToken(tokenRequestParams(authCode));
        GoogleMemberResponse googleMemberResponse =
                googleApiClient.fetchMember("Bearer " + tokenInfo.accessToken());
        return googleMemberResponse.toDomain();
    }

    private MultiValueMap<String, String> tokenRequestParams(String authCode) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", props.clientId());
        params.add("client_secret", props.clientSecret());
        params.add("code", authCode);
        params.add("redirect_uri", props.redirectUri());
        return params;
    }
}

