package com.msa.chat_service.domain.chat.repository;

import com.msa.chat_service.domain.chat.dto.response.ChatMessageResponse;
import com.msa.chat_service.domain.chat.entity.ChatRoom;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import com.msa.chat_service.global.util.NullSafeBuilder;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ChatMessageCustomRepositoryImpl implements ChatMessageCustomRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<ChatMessageResponse> selectChatMessages(ChatRoom chatRoom, Long lastId) {
//        return queryFactory
//                .select(Projections.constructor(ChatMessageResponse.class,
//                        chatMessage.chatRoom.id,
//                        chatMessage.id,
//                        chatMessage.type,
//                        chatMessage.content,
//                        chatMessage.createdAt,
//                        member.id,
//                        member.nickname,
//                        member.profileImage
//                        ))
//                .from(chatMessage)
////                .join(chatMessage.chatRoom, chatRoom)
//                .join(chatMessage.sender, member)
//                .where(isLowerThan(lastId), equalsChatRoom(chatRoom))
//                .orderBy(chatMessage.id.desc())
//                .limit(10)
//                .fetch();
        return null;
    }

//    private BooleanBuilder equalsChatRoom(final ChatRoom chatRoom) {
//        return NullSafeBuilder.build(() -> chatMessage.chatRoom.eq(chatRoom));
//    }
//
//    private BooleanBuilder isLowerThan(final Long chatMessageId) {
//        BooleanBuilder builder = new BooleanBuilder();
//        if (chatMessageId != null && chatMessageId > 0) {
//            builder.and(chatMessage.id.lt(chatMessageId));
//        }
//        return builder;
//    }
}
