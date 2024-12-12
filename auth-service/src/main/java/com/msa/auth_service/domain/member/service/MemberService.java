package com.msa.auth_service.domain.member.service;

import com.msa.auth_service.domain.member.dto.*;
import jakarta.servlet.http.HttpServletResponse;

public interface MemberService {

    void signupMember(MemberSignupRequest signupRequest);

    MemberLoginResponse loginMember(MemberLoginRequest loginRequest, HttpServletResponse response);

    void logoutMember(String email);

    MemberInfo getMember(Long memberId);

    void deleteMember(Long memberId);

    void updateProfileImageAndNickNameMember(Long memberId, MemberUpdateRequest updateRequest);

    void updatePasswordMember(Long memberId, MemberPasswordChangeRequest passwordChangeRequest);
}
