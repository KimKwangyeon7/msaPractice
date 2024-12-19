package com.msa.community_service.domain.community.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CommentListResponse {
    private Long commentId;
    private String content;
    private Long writerId;
    private String writerNickname;
    private String writerProfileImage;
    private LocalDateTime createdAt;


    public CommentListResponse(Long commentId, String content, Long writerId, LocalDateTime createdAt) {
        this.commentId = commentId;
        this.content = content;
        this.writerId = writerId;
        this.createdAt = createdAt;
    }
}

