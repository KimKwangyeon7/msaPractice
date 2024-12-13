package com.msa.member_service.domain.member.service;


import com.msa.member_service.domain.member.dto.MemberInfo;
import com.msa.member_service.domain.member.dto.MemberPasswordChangeRequest;
import com.msa.member_service.domain.member.dto.MemberUpdateRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface MemberService {

    String logoutMember(String email);

//    MemberInfo getMember(Long memberId);
//
//    void deleteMember(Long memberId);
//
//    void updateProfileImageAndNickNameMember(Long memberId, MemberUpdateRequest updateRequest);
//
//    void updatePasswordMember(Long memberId, MemberPasswordChangeRequest passwordChangeRequest);

    String reissueAccessToken(String email);
}
