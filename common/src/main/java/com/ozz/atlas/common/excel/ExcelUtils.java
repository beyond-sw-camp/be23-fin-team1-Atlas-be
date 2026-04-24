package com.ozz.atlas.common.excel;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

// 엑셀 공통 처리 유틸
// 여러 서비스에서 같은 방식으로 셀 값을 읽고,
// 빈 줄인지 확인할 때 재사용
public final class ExcelUtils {

    // 유틸 클래스라서 new 로 만들지 못하게 막음
    private ExcelUtils() {
    }

    // 첫 번째 시트를 안전하게 가져옴
    public static Sheet getFirstSheetOrThrow(org.apache.poi.ss.usermodel.Workbook workbook) {
        // 워크북 자체가 없으면 바로 막음
        if (workbook == null) {
            throw new IllegalArgumentException("엑셀 파일을 읽을 수 없습니다.");
        }

        // 시트가 하나도 없으면 업로드 형식이 잘못된 것으로 봄
        if (workbook.getNumberOfSheets() == 0) {
            throw new IllegalArgumentException("엑셀 시트가 비어 있습니다.");
        }

        // 첫 번째 시트를 반환
        return workbook.getSheetAt(0);
    }

    // 지정한 셀 값을 문자열로 읽음
    public static String getCellValue(Row row, int cellIndex, DataFormatter formatter) {
        // row 가 없으면 빈 문자열로 처리
        if (row == null) {
            return "";
        }

        // formatter 가 없으면 안전하게 새로 만듬
        DataFormatter safeFormatter = formatter != null ? formatter : new DataFormatter();

        // 셀이 비어 있어도 빈 문자열로 처리
        if (row.getCell(cellIndex) == null) {
            return "";
        }

        // 숫자/문자/날짜를 모두 문자열로 바꾸고 앞뒤 공백을 제거
        return safeFormatter.formatCellValue(row.getCell(cellIndex)).trim();
    }

    // 지정한 범위 안에서 이 줄이 전부 비어 있는지 확인
    public static boolean isRowEmpty(Row row, int maxCellIndex, DataFormatter formatter) {
        // row 자체가 없으면 빈 줄
        if (row == null) {
            return true;
        }

        // 0번 칸부터 마지막 확인 칸까지 돌면서 값이 있는지 봄
        for (int cellIndex = 0; cellIndex <= maxCellIndex; cellIndex++) {
            String value = getCellValue(row, cellIndex, formatter);

            // 하나라도 값이 있으면 빈 줄이 아님
            if (!value.isBlank()) {
                return false;
            }
        }

        return true;
    }
}
