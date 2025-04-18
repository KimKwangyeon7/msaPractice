package com.msa.chat_service.global.component.kafka.service;

import com.msa.chat_service.global.component.kafka.dto.info.RankingDataInfo;
import com.msa.chat_service.global.component.kafka.dto.info.WordCountInfo;
import com.msa.chat_service.global.component.kafka.dto.response.RankingResponse;
import com.msa.chat_service.global.component.kafka.dto.response.WordCountResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyWindowStore;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaStreamServiceImpl implements KafkaStreamService {
    private final StreamsBuilderFactoryBean factoryBean;

    @Override
    public RankingResponse getRankings() {
        KafkaStreams kafkaStreams = factoryBean.getKafkaStreams();

        // 시스템에 저장된 시간대로 설정
        ZoneId zoneId = ZoneId.systemDefault();

        LocalDateTime yesterdayMidnight = LocalDate.now(zoneId).minusDays(1).atStartOfDay();
        Instant startOfYesterday = yesterdayMidnight.atZone(zoneId).toInstant();
        Instant now = Instant.now();

        log.info("Fetching data from window store from {} to {}", startOfYesterday, now);

        ReadOnlyWindowStore<String, Long> windowStore = kafkaStreams.store(
                StoreQueryParameters.fromNameAndType("daily-ranking", QueryableStoreTypes.windowStore())
        );

        Map<String, List<RankingDataInfo>> rankingsMap = new HashMap<>();
        rankingsMap.put("District", new ArrayList<>());
        rankingsMap.put("Administration", new ArrayList<>());
        rankingsMap.put("Commercial", new ArrayList<>());
        rankingsMap.put("Service", new ArrayList<>());

        KeyValueIterator<Windowed<String>, Long> iter = windowStore.fetchAll(startOfYesterday, now);

        while (iter.hasNext()) {
            KeyValue<Windowed<String>, Long> entry = iter.next();
            String key = entry.key.key();
            Long count = entry.value;

            log.info(entry.toString());

            String[] parts = key.split(":");
            if (parts.length == 2) {
                String category = parts[0];
                String name = parts[1];
                rankingsMap.get(category).add(new RankingDataInfo(name, count));
            }
        }
        iter.close();

        // Sorting in descending order and returning the rankings
        return new RankingResponse(
                sortDescending(rankingsMap.get("District")),
                sortDescending(rankingsMap.get("Administration")),
                sortDescending(rankingsMap.get("Commercial")),
                sortDescending(rankingsMap.get("Service"))
        );
    }

    private List<RankingDataInfo> sortDescending(List<RankingDataInfo> data) {
        data.sort((o1, o2) -> Long.compare(o2.count(), o1.count()));
        return data;
    }

    @Override
    public WordCountResponse getWordCounts() {
        KafkaStreams kafkaStreams = factoryBean.getKafkaStreams();

        // 시스템 시간대 설정
        ZoneId zoneId = ZoneId.systemDefault();

        LocalDateTime yesterdayMidnight = LocalDate.now(zoneId).minusDays(1).atStartOfDay();
        Instant startOfYesterday = yesterdayMidnight.atZone(zoneId).toInstant();
        Instant now = Instant.now();

        log.info("Fetching word counts from window store from {} to {}", startOfYesterday, now);

        ReadOnlyWindowStore<String, Long> windowStore = kafkaStreams.store(
                StoreQueryParameters.fromNameAndType("daily-word-count-store", QueryableStoreTypes.windowStore())
        );

        List<WordCountInfo> wordCounts = new ArrayList<>();
        KeyValueIterator<Windowed<String>, Long> iter = windowStore.fetchAll(startOfYesterday, now);

        while (iter.hasNext()) {
            KeyValue<Windowed<String>, Long> entry = iter.next();
            String word = entry.key.key();
            Long count = entry.value;

            log.info("Word: {}, Count: {}", word, count);

            wordCounts.add(new WordCountInfo(word, count));
        }
        iter.close();

        // 단어를 빈도수 기준으로 내림차순 정렬하여 반환
        return new WordCountResponse(
                wordCounts.stream()
                        .sorted((o1, o2) -> Long.compare(o2.count(), o1.count()))
                        .collect(Collectors.toList())
        );
    }
}
