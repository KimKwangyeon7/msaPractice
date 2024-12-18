package com.msa.community_service.domain.community.repository;

import com.msa.community_service.domain.community.entity.Community;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityRepository extends JpaRepository<Community, Long>, CommunityCustomRepository {

}
