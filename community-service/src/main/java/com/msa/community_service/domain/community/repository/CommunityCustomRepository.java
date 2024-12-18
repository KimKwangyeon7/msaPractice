package com.msa.community_service.domain.community.repository;

import com.msa.community_service.domain.community.dto.response.CommunityDetailResponse;
import com.msa.community_service.domain.community.dto.response.CommunityListResponse;
import com.msa.community_service.domain.community.dto.response.PopularCommunityListResponse;

import java.util.List;

public interface CommunityCustomRepository {
    List<CommunityListResponse> selectCommunityList(String category, Long lastId);

    CommunityDetailResponse selectCommunity(Long communityId);

    List<PopularCommunityListResponse> selectPopularCommunityList();
}
