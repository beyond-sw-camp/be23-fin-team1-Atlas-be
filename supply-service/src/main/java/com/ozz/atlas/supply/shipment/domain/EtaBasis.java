package com.ozz.atlas.supply.shipment.domain;

public enum EtaBasis {
//    계획ETA(출발전)
    SCHEDULED,
//    실제 출발 시간 기준 계산
    ACTUAL_TRACKING,
//    도착 시간 반환
    ARRIVED
}
