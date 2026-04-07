package com.ozz.atlas.supply.purchaseorder.domain;

public enum PoStatus {
    CREATED, // 생성됨 (초안)
    APPROVED, // 승인됨
    IN_PROGRESS, // 납품 진행중
    COMPLETED, // 전체 입고 완료
    CANCELLED, // 취소
    REJECTED // 반려
}
