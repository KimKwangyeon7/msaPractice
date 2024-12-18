package com.msa.community_service.domain.community.service;

import com.msa.community_service.domain.community.dto.request.UpdateCommentRequest;
import com.msa.community_service.domain.community.dto.response.CommentListResponse;

import java.util.List;

public interface CommentService {
    Long createComment(Long memberId, Long communityId, String content);

    List<CommentListResponse> selectCommentList(Long communityId, Long lastId);

    void deleteComment(Long commentId);

    void updateComment(Long commentId, UpdateCommentRequest request);
}
