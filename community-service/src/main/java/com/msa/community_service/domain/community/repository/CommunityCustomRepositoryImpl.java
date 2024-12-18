package com.msa.community_service.domain.community.repository;

import com.msa.community_service.domain.community.dto.info.ImageInfo;
import com.msa.community_service.domain.community.dto.info.MemberInfoResponse;
import com.msa.community_service.domain.community.dto.response.CommunityDetailResponse;
import com.msa.community_service.domain.community.dto.response.CommunityListResponse;
import com.msa.community_service.domain.community.dto.response.PopularCommunityListResponse;
import com.msa.community_service.domain.community.entity.Community;
import com.msa.community_service.domain.community.entity.QImage;
import com.msa.community_service.global.util.NullSafeBuilder;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Repository;
import com.msa.community_service.domain.community.entity.enums.Category;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.msa.community_service.domain.community.entity.QComments.comments;
import static com.msa.community_service.domain.community.entity.QCommunity.community;
import static com.msa.community_service.domain.community.entity.QImage.image;

@Repository
@RequiredArgsConstructor
public class CommunityCustomRepositoryImpl implements CommunityCustomRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<CommunityListResponse> selectCommunityList(String category, Long lastId) {
        // 1. Community 데이터만 조회
        List<Community> communities = queryFactory
                .selectFrom(community)
                .where(isLowerThan(lastId), equalsCategory(category))
                .orderBy(community.id.desc())
                .limit(10)
                .fetch();

        // 2. writerId로 Member 정보를 REST API 호출하여 조회
        List<Long> writerIds = communities.stream()
                .map(Community::getWriterId)
                .distinct()
                .toList();
        System.out.println(writerIds.get(0) + " " + writerIds.get(1));
        // REST API 호출로 Member 정보를 조회
        Map<Long, MemberInfoResponse> memberInfoMap = getMemberInfoList(writerIds);

        // 3. Community 데이터를 CommunityListResponse로 변환
        List<CommunityListResponse> responses = communities.stream().map(comm -> {
            MemberInfoResponse memberInfo = memberInfoMap.get(comm.getWriterId());
            return new CommunityListResponse(
                    comm.getId(),
                    comm.getCategory(),
                    comm.getTitle(),
                    comm.getContent(),
                    comm.getWriterId(),
                    memberInfo != null ? memberInfo.nickname() : "알 수 없음",
                    memberInfo != null ? memberInfo.profileImage() : null,
                    comm.getReadCount(),
                    getCommentCount(comm.getId()) // Comment 개수 조회
            );
        }).collect(Collectors.toList());

        return responses;
    }

    private int getCommentCount(Long communityId) {
        Integer count = queryFactory
                .select(comments.count().intValue())
                .from(comments)
                .where(comments.community.id.eq(communityId))
                .fetchOne();
        return count != null ? count : 0; // 기본값 0
    }

//    private String getFirstImage(Long communityId) {
//        return queryFactory
//                .select(QImage.image.url)
//                .from(QImage.image)
//                .where(QImage.image.community.id.eq(communityId))
//                .orderBy(QImage.image.id.asc())
//                .limit(1)
//                .fetchOne();
//    }


    private Map<Long, MemberInfoResponse> getMemberInfoList(List<Long> writerIds) {
        // 예제: WebClient 사용
        WebClient webClient = WebClient.create("http://localhost:9443");

        try {
            return webClient.post()
                    .uri("/auth/member/batch") // 배치 조회를 위한 엔드포인트
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
        // Step 1: Community 데이터 조회 (Member 데이터 제외)
        List<PopularCommunityListResponse> responses = queryFactory
                .select(Projections.constructor(PopularCommunityListResponse.class,
                        community.id,
                        community.category,
                        community.title,
                        community.content,
                        community.writerId, // Member 데이터 대신 writerId를 사용
                        community.readCount,
                        ExpressionUtils.as(JPAExpressions.select(comments.count().intValue())
                                .from(comments)
                                .where(comments.community.eq(community)), "commentCount")
                ))
                .from(community)
                .orderBy(community.readCount.desc(), community.id.desc())
                .limit(10)
                .fetch();

        // Step 2: Member 정보 조회를 위한 writerId 수집
        List<Long> writerIds = responses.stream()
                .map(PopularCommunityListResponse::getWriterId)
                .distinct()
                .collect(Collectors.toList());

        // Step 3: Member 서비스 호출
        Map<Long, MemberInfoResponse> memberInfoMap = getMemberInfoList(writerIds);

        // Step 4: Member 정보를 PopularCommunityListResponse에 매핑
        for (PopularCommunityListResponse response : responses) {
            MemberInfoResponse memberInfo = memberInfoMap.get(response.getWriterId());
            if (memberInfo != null) {
                response.setWriterNickname(memberInfo.nickname());
                response.setProfileImage(memberInfo.profileImage());
            }

            // Step 5: 이미지 데이터 추가
            String image = queryFactory.select(QImage.image.url)
                    .from(QImage.image)
                    .where(QImage.image.community.id.eq(response.getCommunityId()))
                    .orderBy(QImage.image.id.asc())
                    .limit(1)
                    .fetchOne();

            response.setImage(image);
        }
        return responses;
    }

    @Override
    public CommunityDetailResponse selectCommunity(Long communityId) {
        // Step 1: Community 데이터 조회
        CommunityDetailResponse communityDetailResponse = queryFactory
                .select(Projections.constructor(CommunityDetailResponse.class,
                        community.id,
                        community.category,
                        community.title,
                        community.content,
                        community.readCount,
                        community.writerId,
                        community.createdAt
                ))
                .from(community)
                .where(equalsCommunityId(communityId))
                .fetchOne();

        // Step 2: writerId 가져오기
        Long writerId = communityDetailResponse.getWriterId();

        // Step 3: Member 정보 조회
        MemberInfoResponse memberInfo = getMemberInfo(writerId);

        // Step 4: Member 정보 매핑
        if (memberInfo != null) {
            communityDetailResponse.setWriterNickname(memberInfo.nickname());
            communityDetailResponse.setWriterProfileImage(memberInfo.profileImage());
        }

        /// Step 5: 이미지 데이터 추가 (여러 이미지 가져오기)
        List<ImageInfo> images = queryFactory.select(Projections.constructor(ImageInfo.class,
                        QImage.image.id,
                        QImage.image.url))
                .from(QImage.image)
                .where(QImage.image.community.id.eq(communityId))
                .orderBy(QImage.image.id.asc())
                .fetch();

        communityDetailResponse.setImages(images);

        return communityDetailResponse;
    }

    private BooleanBuilder equalsCommunityId(final Long communityId) {
        return NullSafeBuilder.build(() -> community.id.eq(communityId));
    }

    private MemberInfoResponse getMemberInfo(Long writerId) {
        WebClient webClient = WebClient.create("http://localhost:9443");

        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/auth//member/{writerId}")
                            .build(writerId))
                    .retrieve()
                    .bodyToMono(MemberInfoResponse.class)
                    .block();
        } catch (Exception e) {
            e.printStackTrace();
            return null; // 실패 시 null 반환
        }
    }
}
