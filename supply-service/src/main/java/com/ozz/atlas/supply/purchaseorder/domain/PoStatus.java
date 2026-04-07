package com.ozz.atlas.supply.purchaseorder.domain;

public enum PoStatus {
    CREATED,              // 발주 생성 직후
    ACCEPTED,             // 협력사가 발주 수락
    PARTIALLY_CONFIRMED,  // 일부 품목만 확정 수량 입력 완료
    CONFIRMED,            // 모든 품목 확정 수량 입력 완료
    REJECTED,             // 협력사가 발주 거절
    CANCELLED,            // 발주사가 발주 취소
    COMPLETED,            // 발주 처리 완료
    DELETED               // 별도 삭제 컬럼이 없어서 논리 삭제로 사용
}
