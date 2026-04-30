package com.ozz.atlas.supply.settlement.domain;

// 예산 사용 상태
public enum BudgetUsageStatus {
    SAFE, //예산 안에서 정상 사용 중
    WARNING, //예산 경고 기준 이상 사용중
    EXCEEDED, // 예산 초과
    NO_BUDGET // 예산 미설정
}
