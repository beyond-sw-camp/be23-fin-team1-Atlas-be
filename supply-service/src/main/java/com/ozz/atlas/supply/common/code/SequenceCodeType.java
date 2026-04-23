package com.ozz.atlas.supply.common.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SequenceCodeType {
    ITEM("ITM"),
    PURCHASE_ORDER("PO"),
    SUB_PURCHASE_ORDER("SUBPO");

    private final String prefix;
}
