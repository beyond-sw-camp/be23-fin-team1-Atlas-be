package com.ozz.atlas.supply.lot.dtos;

import com.ozz.atlas.supply.lot.domain.LotStatus;
import com.ozz.atlas.supply.lot.domain.QualityStatus;
import com.ozz.atlas.supply.lot.domain.LotStatusHistory;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class LotHistoryResponseDto {
    private String publicId;
    private LotStatus preLotStatus;
    private LotStatus lotStatus;
    private QualityStatus preQualityStatus;
    private QualityStatus qualityStatus;
    private String preNodePublicId;
    private String currentNodePublicId;
    private String reason;
    private LocalDateTime createdAt;

    public static LotHistoryResponseDto from(LotStatusHistory history) {
        return LotHistoryResponseDto.builder()
                .publicId(history.getPublicId())
                .preLotStatus(history.getPreLotStatus())
                .lotStatus(history.getLotStatus())
                .preQualityStatus(history.getPreQualityStatus())
                .qualityStatus(history.getQualityStatus())
                .preNodePublicId(history.getPreNodePublicId())
                .currentNodePublicId(history.getCurrentNodePublicId())
                .reason(history.getReason())
                .createdAt(history.getCreatedAt())
                .build();
    }
}
