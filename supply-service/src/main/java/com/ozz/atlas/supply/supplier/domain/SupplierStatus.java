package com.ozz.atlas.supply.supplier.domain;

public enum SupplierStatus {
    ACTIVE, // 정상 거래중
    INACTIVE, // 비활성 (잠시)
    SUSPENDED, // 일시 정지
    TERMINATED // 계약 해지
}