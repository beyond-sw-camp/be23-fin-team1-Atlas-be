package com.ozz.atlas.supply.common.code;

import java.time.Year;
import java.time.ZoneId;

public final class YearlySequenceCodeGenerator {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private YearlySequenceCodeGenerator() {
    }

    public static String currentPrefix(SequenceCodeType type) {
        return type.getPrefix() + "-" + Year.now(KST).getValue() + "-";
    }

    public static String next(SequenceCodeType type, String lastCode, int digits) {
        String prefix = currentPrefix(type);
        int lastSequence = lastCode != null && lastCode.startsWith(prefix)
                ? extractSequence(lastCode)
                : 0;

        return prefix + String.format("%0" + digits + "d", lastSequence + 1);
    }

    public static int extractSequence(String code) {
        try {
            return Integer.parseInt(code.substring(code.lastIndexOf('-') + 1));
        } catch (RuntimeException e) {
            return 0;
        }
    }
}