package com.ozz.atlas.supply.supplier.capability.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

// DB에는 문자열, 여기서는 enum
@Converter
public class SupplierItemQualityGradeConverter implements AttributeConverter<SupplierItemQualityGrade, String> {

    @Override
    public String convertToDatabaseColumn(SupplierItemQualityGrade attribute) {
        return attribute != null ? attribute.getDbValue() : null;
    }

    @Override
    public SupplierItemQualityGrade convertToEntityAttribute(String dbData) {
        return dbData != null ? SupplierItemQualityGrade.fromDbValue(dbData) : null;
    }
}
