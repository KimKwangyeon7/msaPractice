package com.msa.auth_service.domain.member.repository;


import com.msa.auth_service.domain.member.dto.MemberInfoResponse;
import com.msa.auth_service.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByEmail(String email);

    Optional<Member> findByEmail(String email);

    @Query(value = """
    SELECT new com.msa.auth_service.domain.member.dto.MemberInfoResponse(
        m.id, m.nickname, m.profileImage) FROM Member m WHERE m.id IN :ids
    """)
    List<MemberInfoResponse> getMemberInfoResponsesList(@Param("ids") List<Long> ids);

    @Query(value = """
    SELECT new com.msa.auth_service.domain.member.dto.MemberInfoResponse(
        m.id, m.nickname, m.profileImage) FROM Member m WHERE m.id = :writerId
    """)
    MemberInfoResponse getMemberInfoResponse(@Param("writerId") Long writerId);
}
