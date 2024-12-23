package com.msa.chat_service.domain.chat.repository;

import com.msa.chat_service.domain.chat.dto.response.ChatMessageResponse;
import com.msa.chat_service.domain.chat.entity.ChatRoom;
import com.msa.chat_service.domain.member.dto.MemberInfoResponse;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Repository;
import com.msa.chat_service.global.util.NullSafeBuilder;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.msa.chat_service.domain.chat.entity.QChatMessage.chatMessage;

@Repository
@RequiredArgsConstructor
public class ChatMessageCustomRepositoryImpl implements ChatMessageCustomRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<ChatMessageResponse> selectChatMessages(ChatRoom chatRoom, Long lastId) {
        List<ChatMessageResponse> responses = queryFactory
                .select(Projections.constructor(ChatMessageResponse.class,
                        chatMessage.chatRoom.id,
                        chatMessage.id,
                        chatMessage.type,
                        chatMessage.content,
                        chatMessage.createdAt,
                        chatMessage.memberId
                        ))
                .from(chatMessage)
                .where(isLowerThan(lastId), equalsChatRoom(chatRoom))
                .orderBy(chatMessage.id.desc())
                .limit(10)
                .fetch();

        // Step 2: Member 정보 조회를 위한 writerId 수집
        List<Long> writerIds = responses.stream()
                .map(ChatMessageResponse::getSenderId)
                .distinct()
                .collect(Collectors.toList());

        // Step 3: Member 서비스 호출
        Map<Long, MemberInfoResponse> memberInfoMap = getMemberInfoList(writerIds);

        // Step 4: Member 정보를 PopularCommunityListResponse에 매핑
        for (ChatMessageResponse response : responses) {
            MemberInfoResponse memberInfo = memberInfoMap.get(response.getSenderId());
            if (memberInfo != null) {
                response.setSenderNickname(memberInfo.nickname());
                response.setSenderProfileImage(memberInfo.profileImage());
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

    private BooleanBuilder equalsChatRoom(final ChatRoom chatRoom) {
        return NullSafeBuilder.build(() -> chatMessage.chatRoom.eq(chatRoom));
    }

    private BooleanBuilder isLowerThan(final Long chatMessageId) {
        BooleanBuilder builder = new BooleanBuilder();
        if (chatMessageId != null && chatMessageId > 0) {
            builder.and(chatMessage.id.lt(chatMessageId));
        }
        return builder;
    }
}
