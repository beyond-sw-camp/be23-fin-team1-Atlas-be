package com.ozz.atlas.supply.batch.domain;

// 유통기한 남은 기간 구간
public enum LotExpiryBucket {
    EXPIRED, // 유통기한 지남
    D0_7, // 0~7일 남음
    D8_30, // 8~30일 남음
    D31_PLUS, // 31일 이상 남음
    NO_EXPIRY; // 유통기한 계산 불가

    public static LotExpiryBucket fromDaysRemaining(long daysRemaining) {
        if (daysRemaining < 0) {
            return EXPIRED;
        }
        if (daysRemaining <= 7) {
            return D0_7;
        }
        if (daysRemaining <= 30) {
            return D8_30;
        }
        return D31_PLUS;
    }
}
