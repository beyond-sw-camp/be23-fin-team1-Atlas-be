package com.ozz.atlas.supply.supplier.esg.domain;

import java.math.BigDecimal;

public enum EsgGrade {
    A, B, C, D, E;

    public static EsgGrade fromScore(BigDecimal totalScore) {
        if (totalScore.compareTo(new BigDecimal("90.00")) >= 0) return A;
        if (totalScore.compareTo(new BigDecimal("80.00")) >= 0) return B;
        if (totalScore.compareTo(new BigDecimal("70.00")) >= 0) return C;
        if (totalScore.compareTo(new BigDecimal("60.00")) >= 0) return D;
        return E;
    }
}
