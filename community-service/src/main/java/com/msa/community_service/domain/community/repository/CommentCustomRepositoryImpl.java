package com.msa.community_service.domain.community.repository;


import com.msa.community_service.domain.community.dto.info.MemberInfoResponse;
import com.msa.community_service.domain.community.dto.response.CommentListResponse;
import com.msa.community_service.domain.community.dto.response.PopularCommunityListResponse;
import com.msa.community_service.domain.community.entity.QImage;
import com.msa.community_service.global.util.NullSafeBuilder;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.msa.community_service.domain.community.entity.QComments.comments;


@Repository
@RequiredArgsConstructor
public class CommentCustomRepositoryImpl implements CommentCustomRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<CommentListResponse> selectCommentList(Long communityId, Long lastId) {
        // Comments 데이터만 조회
        List<CommentListResponse> responses = queryFactory
                .select(Projections.constructor(CommentListResponse.class,
                        comments.id,
                        comments.content,
                        comments.writerId,
                        comments.createdAt
                ))
                .from(comments)
                .where(isLowerThan(lastId), equalsCommunityId(communityId))
                .orderBy(comments.id.desc())
                .limit(10)
                .fetch();

        // Member 정보 조회를 위한 writerId 수집
        List<Long> writerIds = responses.stream()
                .map(CommentListResponse::getWriterId)
                .distinct()
                .collect(Collectors.toList());

        // Member 서비스 호출
        Map<Long, MemberInfoResponse> memberInfoMap = getMemberInfoList(writerIds);

        // Member 정보를 PopularCommunityListResponse에 매핑
        for (CommentListResponse response : responses) {
            MemberInfoResponse memberInfo = memberInfoMap.get(response.getWriterId());
            if (memberInfo != null) {
                response.setWriterNickname(memberInfo.nickname());
                response.setWriterProfileImage(memberInfo.profileImage());
            }
        }
        return responses;
    }

    private Map<Long, MemberInfoResponse> getMemberInfoList(List<Long> writerIds) {
        // 예제: WebClient 사용
        WebClient webClient = WebClient.create("http://localhost:9443");

        try {
            return webClient.post()
                    .uri("/auth/member/community") // 배치 조회를 위한 엔드포인트
                    .bodyValue(writerIds)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<MemberInfoResponse>>() {})
                    .block()
                    .stream()
                    .collect(Collectors.toMap(MemberInfoResponse::id, member -> member));
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyMap(); // 실패 시 빈 맵 반환
        }
    }

    private BooleanBuilder equalsCommunityId(final Long communityId) {
        return NullSafeBuilder.build(() -> comments.community.id.eq(communityId));
    }

    private BooleanBuilder isLowerThan(final Long commentId) {
        BooleanBuilder builder = new BooleanBuilder();
        if (commentId != null && commentId > 0) {
            builder.and(comments.id.lt(commentId));
        }
        return builder;
    }
}
