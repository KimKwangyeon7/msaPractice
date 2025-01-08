package com.msa.chat_service.global.component.kafka;

import lombok.Getter;

@Getter
public class KafkaConstants {
    public static final String KAFKA_TOPIC = "chat.room.message.sending";
    public static final String KAFKA_TOPIC_CHAT_COUNT = "daily.word.count.store";
}
