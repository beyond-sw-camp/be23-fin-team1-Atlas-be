package com.ozz.atlas.supply.supplier.relation.domain;

public enum SupplierRelationStatus {
    REQUESTED,  // 발주 요청 상태
    ACTIVE,     // 거래중
    PAUSED,  // 거래 일시중단
    ENDED       // 거래종료
}