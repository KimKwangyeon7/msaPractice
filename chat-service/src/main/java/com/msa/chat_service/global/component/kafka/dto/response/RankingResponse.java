package com.msa.chat_service.global.component.kafka.dto.response;


import com.msa.chat_service.global.component.kafka.dto.info.RankingDataInfo;

import java.util.List;

public record RankingResponse(
        List<RankingDataInfo> districtRankings,
        List<RankingDataInfo> administrationRankings,
        List<RankingDataInfo> commercialRankings,
        List<RankingDataInfo> serviceRankings
) {
}
