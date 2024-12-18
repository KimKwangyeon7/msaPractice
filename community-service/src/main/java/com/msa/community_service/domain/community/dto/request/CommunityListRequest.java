package com.msa.community_service.domain.community.dto.request;

public record CommunityListRequest(
        String category,
        Long lastId
) {
}
