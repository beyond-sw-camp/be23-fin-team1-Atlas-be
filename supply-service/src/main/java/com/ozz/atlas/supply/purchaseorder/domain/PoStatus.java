package com.ozz.atlas.supply.purchaseorder.domain;

public enum PoStatus {
    CREATED,              // 발주 생성
    PARTIALLY_CONFIRMED,  // 일부 품목 확정
    CONFIRMED,            // 모든 품목 확정
    REJECTED,             // 협력사 거절
    CANCELLED,            // 발주사 취소
    COMPLETED,            // 발주 처리 완료
    DELETED               // 논리 삭제
}
