package com.ozz.atlas.supply.supplier.capability.domain;

public enum SupplierItemQualityGrade {
    AAA("AAA"),
    AA_PLUS("AA+"),
    AA("AA"),
    A_PLUS("A+"),
    A("A"),
    B("B"),
    C("C");

    private final String dbValue;

    SupplierItemQualityGrade(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static SupplierItemQualityGrade fromDbValue(String dbValue) {
        for (SupplierItemQualityGrade value : values()) {
            if (value.dbValue.equals(dbValue)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown quality grade: " + dbValue);
    }
}
