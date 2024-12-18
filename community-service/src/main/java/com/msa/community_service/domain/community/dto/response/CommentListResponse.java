package com.msa.community_service.domain.community.dto.response;

import java.time.LocalDateTime;

public record CommentListResponse(
        Long commentId,
        String content,
        Long writerId,
        String writerNickname,
        String writerProfileImage,
        LocalDateTime createdAt
) {
}
