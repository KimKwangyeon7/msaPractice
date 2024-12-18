package com.msa.community_service.domain.community.repository;

import com.msa.community_service.domain.community.entity.Comments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface CommentRepository extends JpaRepository<Comments, Long>, CommentCustomRepository {
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Comments c WHERE c.community.id = :communityId")
    void deleteByCommunityId(Long communityId);


}
