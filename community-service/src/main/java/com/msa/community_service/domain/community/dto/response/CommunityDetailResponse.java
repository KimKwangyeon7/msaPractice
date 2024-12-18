package com.msa.community_service.domain.community.dto.response;


import com.msa.community_service.domain.community.dto.info.ImageInfo;
import com.msa.community_service.domain.community.entity.enums.Category;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommunityDetailResponse {
    private Long communityId;
    private Category category;
    private String title;
    private String content;
    private int readCount;
    private Long writerId;
    private String writerNickname;
    private String writerProfileImage;
    private LocalDateTime createdAt;
    private List<ImageInfo> images;


    public CommunityDetailResponse(Long communityId, Category category, String title, String content, int readCount, Long writerId, LocalDateTime createdAt) {
        this.communityId = communityId;
        this.category = category;
        this.title = title;
        this.content = content;
        this.readCount = readCount;
        this.writerId = writerId;
        this.createdAt = createdAt;
    }

}
