package com.msa.community_service.domain.community.repository;

import com.msa.community_service.domain.community.dto.response.CommentListResponse;
import java.util.List;

public interface CommentCustomRepository {
    List<CommentListResponse> selectCommentList(Long communityId, Long lastId);
}
