package com.msa.chat_service.global.component.kafka.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msa.chat_service.global.component.kafka.serde.JsonSerde;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.WindowStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class WordCountProcessor {
    private static final Serde<String> STRING_SERDE = Serdes.String(); // 문자열 Serde 설정
    private static final Serde<Long> LONG_SERDE = Serdes.Long(); // Long Serde 설정

    @Autowired
    void buildPipeline(StreamsBuilder streamsBuilder) {
        // Input Topic 정의
        KStream<String, String> chatStream = streamsBuilder
                .stream("chat.room.message.sending", Consumed.with(STRING_SERDE, STRING_SERDE));

        // 메시지에서 content만 추출
        KStream<String, String> contentStream = chatStream
                .mapValues(this::extractContent)
                .filter((key, value) -> value != null && !value.isBlank()); // content가 null 또는 빈 값이면 필터링

        // 로그 출력으로 content 확인
        contentStream.peek((key, value) -> log.info("Filtered content: {}", value));

        // 하루 윈도우 설정
        TimeWindows dailyWindow = TimeWindows.ofSizeAndGrace(Duration.ofDays(1), Duration.ofMinutes(5));

        // 메시지를 단어 단위로 나누고, 빈도를 계산하여 KTable 생성
        KTable<Windowed<String>, Long> wordCounts = contentStream
                .flatMapValues(this::splitMessageIntoWords) // 메시지를 단어로 분리
                .groupBy((key, word) -> word, Grouped.with(STRING_SERDE, STRING_SERDE)) // 단어를 그룹화
                .windowedBy(dailyWindow) // 하루 단위 윈도우 적용
                .count(Materialized.<String, Long, WindowStore<Bytes, byte[]>>as("daily-word-count-store")
                        .withKeySerde(STRING_SERDE)
                        .withValueSerde(LONG_SERDE)); // 상태 저장소에 저장

        // KTable -> KStream으로 변환 후 Output Topic으로 전송
        wordCounts.toStream()
                .to("daily-word-counts", Produced.with(WindowedSerdes.timeWindowedSerdeFrom(String.class), LONG_SERDE));

        log.info("Chat Word Count Processor initialized.");
    }

    /**
     * JSON 메시지에서 content 필드를 추출하는 메서드
     */
    private String extractContent(String message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(message);

            // content 필드 추출
            if (rootNode.has("content")) {
                return rootNode.get("content").asText();
            } else {
                log.warn("Missing 'content' field in JSON: {}", message);
            }
        } catch (Exception e) {
            log.error("Failed to parse JSON message: {}", message, e);
        }

        return ""; // content 필드가 없거나 파싱 실패 시 빈 문자열 반환
    }

    /**
     * 메시지를 단어로 분리하는 메소드
     */
    private List<String> splitMessageIntoWords(String content) {
        // 한글과 영어를 포함한 단어 분리 (유니코드 정규식 사용)
        List<String> words = Arrays.asList(content.toLowerCase().split("[^\\p{L}\\p{N}]+"));

        // 빈 문자열 제거
        words = words.stream().filter(word -> !word.isBlank()).toList();

        // 분리된 단어 로그 출력
        log.info("Extracted words: {}", words);
        return words;
    }
//    private static final Serde<String> STRING_SERDE = Serdes.String();  // 문자열 Serde 설정
//    private static final Serde<CommercialAnalysisKafkaRequest> COMMERCIAL_ANALYSIS_RESPONSE_SERDE = JsonSerde.forType(CommercialAnalysisKafkaRequest.class);
//
//    @Autowired
//    void buildPipeline(StreamsBuilder streamsBuilder) {
//        KStream<String, CommercialAnalysisKafkaRequest> messageStream = streamsBuilder
//                .stream("commercial-analysis", Consumed.with(STRING_SERDE, COMMERCIAL_ANALYSIS_RESPONSE_SERDE));
//
//        // 하루 윈도우 설정
//        TimeWindows dailyWindow = TimeWindows.ofSizeAndGrace(Duration.ofDays(1), Duration.ZERO);
//
//        // Apply windowed operation
//        KTable<Windowed<String>, Long> wordCounts = messageStream
//                .flatMapValues(this::extractAndCategorizeValues)
//                .groupBy((key, word) -> word, Grouped.with(STRING_SERDE, STRING_SERDE))
//                .windowedBy(dailyWindow)
//                .count(Materialized.as("daily-ranking"));
//
//        // 추출한 windowSize를 기반으로 Serde 생성
//        long windowSize = Duration.ofDays(1).toMillis(); // 하루 단위의 밀리세컨드
//
//
//        wordCounts.toStream()
//                .to("daily-analysis-output", Produced.with(WindowedSerdes.timeWindowedSerdeFrom(String.class, windowSize), Serdes.Long()));
//    }
//
//    private List<String> extractAndCategorizeValues(CommercialAnalysisKafkaRequest value) {
//        List<String> categorizedWords = new ArrayList<>();
//        categorizedWords.add("District:" + value.districtCodeName());
//        categorizedWords.add("Administration:" + value.administrationCodeName());
//        categorizedWords.add("Commercial:" + value.commercialCodeName());
//        categorizedWords.add("Service:" + value.serviceCodeName());
//        log.info("Categorized Values: {}", categorizedWords);
//        return categorizedWords;
//    }

}
