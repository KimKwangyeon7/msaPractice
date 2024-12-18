package com.msa.community_service.domain.community.repository;


import com.msa.community_service.domain.community.dto.response.CommentListResponse;
import com.msa.community_service.global.util.NullSafeBuilder;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.msa.community_service.domain.community.entity.QComments.comments;


@Repository
@RequiredArgsConstructor
public class CommentCustomRepositoryImpl implements CommentCustomRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<CommentListResponse> selectCommentList(Long communityId, Long lastId) {
//        return queryFactory
//                .select(Projections.constructor(CommentListResponse.class,
//                        comments.id,
//                        comments.content,
//                        comments.writer.id,
//                        comments.writer.nickname,
//                        comments.writer.profileImage,
//                        comments.createdAt
//                ))
//                .from(comments)
//                .join(comments.writer, member)
//                .where(equalsCommunityId(communityId), isLowerThan(lastId))
//                .orderBy(comments.id.desc())
//                .limit(10)
//                .fetch();
        return null;
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
