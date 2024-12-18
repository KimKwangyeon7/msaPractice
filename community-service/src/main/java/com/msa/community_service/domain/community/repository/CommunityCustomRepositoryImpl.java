package com.msa.community_service.domain.community.repository;

import com.msa.community_service.domain.community.dto.info.ImageInfo;
import com.msa.community_service.domain.community.dto.response.CommunityDetailResponse;
import com.msa.community_service.domain.community.dto.response.CommunityListResponse;
import com.msa.community_service.domain.community.dto.response.PopularCommunityListResponse;
import com.msa.community_service.domain.community.entity.QImage;
import com.msa.community_service.global.util.NullSafeBuilder;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import com.msa.community_service.domain.community.entity.enums.Category;

import java.util.List;

import static com.msa.community_service.domain.community.entity.QComments.comments;
import static com.msa.community_service.domain.community.entity.QCommunity.community;

@Repository
@RequiredArgsConstructor
public class CommunityCustomRepositoryImpl implements CommunityCustomRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<CommunityListResponse> selectCommunityList(String category, Long lastId) {
//        List<CommunityListResponse> responses = queryFactory
//                .select(Projections.constructor(CommunityListResponse.class,
//                        community.id,
//                        community.category,
//                        community.title,
//                        community.content,
//                        member.id,
//                        member.nickname,
//                        member.profileImage,
//                        community.readCount,
//                        ExpressionUtils.as(JPAExpressions.select(comments.count().intValue())
//                                .from(comments)
//                                .where(comments.community.eq(community)), "commentCount")
//                ))
//                .from(community)
//                .join(community.writer, member)
//                .where(isLowerThan(lastId), equalsCategory(category))
//                .orderBy(community.id.desc())
//                .limit(10)
//                .fetch();
//
//        for (CommunityListResponse response : responses) {
//            String image = queryFactory.select(QImage.image.url)
//                    .from(QImage.image)
//                    .where(QImage.image.community.id.eq(response.getCommunityId()))
//                    .orderBy(QImage.image.id.asc())
//                    .limit(1)
//                    .fetchOne();
//
//            response.setImage(image);
//        }
//
//        return responses;
        return null;
    }

    private BooleanBuilder isLowerThan(final Long communityId) {
        BooleanBuilder builder = new BooleanBuilder();
        if (communityId != null && communityId > 0) {
            builder.and(community.id.lt(communityId));
        }
        return builder;
    }

    private BooleanBuilder equalsCategory(final String category) {
        BooleanBuilder builder = new BooleanBuilder();
        if (!category.isBlank()) {
            builder.and(community.category.eq(Category.valueOf(category)));
        }
        return builder;
    }

    @Override
    public List<PopularCommunityListResponse> selectPopularCommunityList() {
//        List<PopularCommunityListResponse> responses = queryFactory
//                .select(Projections.constructor(PopularCommunityListResponse.class,
//                        community.id,
//                        community.category,
//                        community.title,
//                        community.content,
//                        member.id,
//                        member.nickname,
//                        member.profileImage,
//                        community.readCount,
//                        ExpressionUtils.as(JPAExpressions.select(comments.count().intValue())
//                                .from(comments)
//                                .where(comments.community.eq(community)), "commentCount")
//                ))
//                .from(community)
//                .join(community.writer, member)
//                .orderBy(community.readCount.desc(), community.id.desc())
//                .limit(10)
//                .fetch();
//
//        for (PopularCommunityListResponse response : responses) {
//            String image = queryFactory.select(QImage.image.url)
//                    .from(QImage.image)
//                    .where(QImage.image.community.id.eq(response.getCommunityId()))
//                    .orderBy(QImage.image.id.asc())
//                    .limit(1)
//                    .fetchOne();
//
//            response.setImage(image);
//        }
//
//        return responses;
        return null;
    }

    @Override
    public CommunityDetailResponse selectCommunity(Long communityId) {
//        CommunityDetailResponse communityDetailResponse = queryFactory
//                .select(Projections.constructor(CommunityDetailResponse.class,
//                        community.id,
//                        community.category,
//                        community.title,
//                        community.content,
//                        community.readCount,
//                        community.writer.id,
//                        community.writer.nickname,
//                        community.writer.profileImage,
//                        community.createdAt
//                ))
//                .from(community)
//                .where(equalsCommunityId(communityId))
//                .fetchOne();
//
//        List<ImageInfo> images = queryFactory
//                .select(Projections.constructor(ImageInfo.class,
//                        image.id,
//                        image.url
//                ))
//                .from(image)
//                .where(equalsCommunityId(communityId))
////                .where(image.community.id.eq(communityId))
//                .fetch();
//
//        communityDetailResponse.setImages(images);
//
//        return communityDetailResponse;
        return null;
    }

    private BooleanBuilder equalsCommunityId(final Long communityId) {
        return NullSafeBuilder.build(() -> community.id.eq(communityId));
    }
}
