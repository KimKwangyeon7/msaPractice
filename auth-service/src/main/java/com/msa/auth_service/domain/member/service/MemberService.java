package com.msa.auth_service.domain.member.service;

import com.msa.auth_service.domain.member.dto.*;
import jakarta.servlet.http.HttpServletResponse;

public interface MemberService {

    void signupMember(MemberSignupRequest signupRequest);

    MemberLoginResponse loginMember(MemberLoginRequest loginRequest, HttpServletResponse response);

    void logoutMember(String email);

    void deleteMember(String email);

    void updateProfileImageAndNickNameMember(Long memberId, MemberUpdateRequest updateRequest);

    void updatePasswordMember(Long memberId, MemberPasswordChangeRequest passwordChangeRequest);
}
