package com.msa.community_service.domain.community.service;


import com.msa.community_service.domain.community.dto.request.UpdateCommentRequest;
import com.msa.community_service.domain.community.dto.response.CommentListResponse;
import com.msa.community_service.domain.community.entity.Comments;
import com.msa.community_service.domain.community.entity.Community;
import com.msa.community_service.domain.community.exception.CommunityErrorCode;
import com.msa.community_service.domain.community.exception.CommunityException;
import com.msa.community_service.domain.community.repository.CommentRepository;
import com.msa.community_service.domain.community.repository.CommunityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final CommunityRepository communityRepository;

    @Override
    public Long createComment(Long memberId, Long communityId, String content) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new CommunityException(CommunityErrorCode.NOT_EXIST_COMMUNITY));

        Comments comment = Comments.builder()
                .writerId(memberId)
                .community(community)
                .content(content)
                .build();

        commentRepository.save(comment);

        return comment.getId();
    }

    @Override
    public List<CommentListResponse> selectCommentList(Long communityId, Long lastId) {
        return commentRepository.selectCommentList(communityId, lastId);
    }

    @Transactional
    @Override
    public void deleteComment(Long commentId) {
        commentRepository.deleteById(commentId);
    }

    @Transactional
    @Override
    public void updateComment(Long commentId, UpdateCommentRequest request) {
        Comments comments = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommunityException(CommunityErrorCode.NOT_EXIST_COMMENT));

        comments.update(request.content());
    }
}
