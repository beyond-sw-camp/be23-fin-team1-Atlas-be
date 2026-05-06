package com.ozz.atlas.common.excel;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;

// 엑셀 다운로드 파일을 만들 때 공통으로 쓰는 유틸입니다.
public final class ExcelExportUtils {

    private static final byte[] HEADER_BG = {(byte) 0x1F, (byte) 0x35, (byte) 0x64};

    private static final byte[] ROW_EVEN_BG = {(byte) 0xE8, (byte) 0xF0, (byte) 0xFE};

    private static final byte[] ROW_ODD_BG = {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF};

    private static final byte[] SUBTOTAL_BG = {(byte) 0xFF, (byte) 0xF3, (byte) 0xCD};

    private static final byte[] SETTLEMENT_GRAY_BG = {(byte) 0xD9, (byte) 0xD9, (byte) 0xD9};

    private static final byte[] SETTLEMENT_WHITE_BG = {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF};

    private ExcelExportUtils() {
    }

    private static void applyBorder(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }

    private static void applyRgbFill(XSSFCellStyle style, byte[] rgb) {
        XSSFColor color = new XSSFColor(rgb, null);
        style.setFillForegroundColor(color);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    }

    public static CellStyle createHeaderStyle(Workbook workbook) {
        XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();

        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);

        applyRgbFill(style, HEADER_BG);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        applyBorder(style);

        return style;
    }

    public static CellStyle createCompanyTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);

        return style;
    }

    public static CellStyle createBodyStyle(Workbook workbook) {
        XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();

        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);

        applyRgbFill(style, ROW_ODD_BG);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        applyBorder(style);

        return style;
    }

    public static CellStyle createStripedBodyStyle(Workbook workbook) {
        XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();

        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);

        applyRgbFill(style, ROW_EVEN_BG);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        applyBorder(style);

        return style;
    }

    public static CellStyle createSubtotalStyle(Workbook workbook) {
        XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();

        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);

        applyRgbFill(style, SUBTOTAL_BG);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        applyBorder(style);

        return style;
    }

    public static CellStyle createAmountStyle(Workbook workbook, CellStyle baseStyle) {
        CellStyle style = workbook.createCellStyle();
        style.cloneStyleFrom(baseStyle);

        DataFormat dataFormat = workbook.createDataFormat();
        style.setDataFormat(dataFormat.getFormat("#,##0;[Red]-#,##0"));
        style.setAlignment(HorizontalAlignment.RIGHT);

        return style;
    }

    public static CellStyle createAmountStyle(Workbook workbook) {
        return createAmountStyle(workbook, createBodyStyle(workbook));
    }

    public static void writeHeader(Row row, String[] headers, CellStyle headerStyle) {
        for (int col = 0; col < headers.length; col++) {
            Cell cell = row.createCell(col);
            cell.setCellValue(headers[col]);

            if (headerStyle != null) {
                cell.setCellStyle(headerStyle);
            }
        }

        row.setHeightInPoints(22);
    }

    public static void writeHeader(Row row, String[] headers, CellStyle style, int startCol) {
        for (int i = 0; i < headers.length; i++) {
            writeCell(row, startCol + i, headers[i], style);
        }
    }

    public static void writeCell(Row row, int columnIndex, Object value) {
        Cell cell = row.createCell(columnIndex);

        if (value == null) {
            cell.setCellValue("");
            return;
        }

        if (value instanceof BigDecimal decimalValue) {
            cell.setCellValue(decimalValue.doubleValue());
            return;
        }

        if (value instanceof Number numberValue) {
            cell.setCellValue(numberValue.doubleValue());
            return;
        }

        cell.setCellValue(String.valueOf(value));
    }

    public static void writeCell(Row row, int columnIndex, Object value, CellStyle style) {
        writeCell(row, columnIndex, value);

        if (style != null) {
            row.getCell(columnIndex).setCellStyle(style);
        }
    }

    public static void writeMergedCell(
            Sheet sheet,
            int rowIndex,
            int firstCol,
            int lastCol,
            Object value,
            CellStyle style
    ) {
        Row row = sheet.getRow(rowIndex);

        if (row == null) {
            row = sheet.createRow(rowIndex);
        }

        for (int col = firstCol; col <= lastCol; col++) {
            writeCell(row, col, "", style);
        }

        writeCell(row, firstCol, value, style);

        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(
                rowIndex,
                rowIndex,
                firstCol,
                lastCol
        ));
    }

    public static void autoSizeColumns(Sheet sheet, int columnCount) {
        for (int col = 0; col < columnCount; col++) {
            sheet.autoSizeColumn(col);

            int adjusted = (int) (sheet.getColumnWidth(col) * 1.3) + 1024;
            sheet.setColumnWidth(col, Math.min(adjusted, 15000));
        }
    }

    public static byte[] toByteArray(Workbook workbook) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("엑셀 파일 생성에 실패했습니다.", e);
        }
    }

    public static CellStyle createDocumentTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();

        font.setFontName("맑은 고딕");
        font.setFontHeightInPoints((short) 20);
        font.setBold(true);
        font.setColor(IndexedColors.BLACK.getIndex());

        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        return style;
    }

    public static CellStyle createIssuedDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();

        font.setFontName("맑은 고딕");
        font.setFontHeightInPoints((short) 9);
        font.setColor(IndexedColors.GREY_50_PERCENT.getIndex());

        style.setFont(font);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        return style;
    }

    public static CellStyle createSectionTitleStyle(Workbook workbook) {
        CellStyle style = settlementGrayFillStyle(workbook, true, (short) 10);
        setMediumBorder(style);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    public static CellStyle createInfoLabelStyle(Workbook workbook) {
        CellStyle style = settlementGrayFillStyle(workbook, true, (short) 9);
        setThinBorder(style);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    public static CellStyle createInfoValueStyle(Workbook workbook) {
        CellStyle style = settlementWhiteFillStyle(workbook, false, (short) 9);
        setThinBorder(style);
        style.setAlignment(HorizontalAlignment.LEFT);
        return style;
    }

    public static CellStyle createSummaryLabelStyle(Workbook workbook) {
        CellStyle style = settlementGrayFillStyle(workbook, true, (short) 10);
        setThinBorder(style);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    public static CellStyle createSummaryAmountStyle(Workbook workbook) {
        CellStyle style = settlementWhiteFillStyle(workbook, true, (short) 12);
        setThinBorder(style);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0;[Red]-#,##0"));

        return style;
    }

    public static CellStyle createSettlementTableHeaderStyle(Workbook workbook) {
        CellStyle style = settlementGrayFillStyle(workbook, true, (short) 9);
        setMediumBorder(style);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    public static CellStyle createSettlementTableBodyStyle(Workbook workbook) {
        CellStyle style = settlementWhiteFillStyle(workbook, false, (short) 9);
        setThinBorder(style);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    public static CellStyle createSettlementTableAmountStyle(Workbook workbook) {
        CellStyle style = settlementWhiteFillStyle(workbook, false, (short) 9);
        setThinBorder(style);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0;[Red]-#,##0"));
        return style;
    }

    public static CellStyle createSettlementTotalLabelStyle(Workbook workbook) {
        CellStyle style = settlementGrayFillStyle(workbook, true, (short) 10);
        setThinBorder(style);
        style.setBorderTop(BorderStyle.MEDIUM);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    public static CellStyle createSettlementTotalAmountStyle(Workbook workbook) {
        CellStyle style = settlementGrayFillStyle(workbook, true, (short) 10);
        setThinBorder(style);
        style.setBorderTop(BorderStyle.MEDIUM);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0;[Red]-#,##0"));
        return style;
    }

    private static CellStyle settlementGrayFillStyle(Workbook workbook, boolean bold, short fontSize) {
        XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();
        Font font = workbook.createFont();

        font.setFontName("맑은 고딕");
        font.setFontHeightInPoints(fontSize);
        font.setBold(bold);
        font.setColor(IndexedColors.BLACK.getIndex());

        style.setFont(font);
        applyRgbFill(style, SETTLEMENT_GRAY_BG);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        return style;
    }

    private static CellStyle settlementWhiteFillStyle(Workbook workbook, boolean bold, short fontSize) {
        XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();
        Font font = workbook.createFont();

        font.setFontName("맑은 고딕");
        font.setFontHeightInPoints(fontSize);
        font.setBold(bold);
        font.setColor(IndexedColors.BLACK.getIndex());

        style.setFont(font);
        applyRgbFill(style, SETTLEMENT_WHITE_BG);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        return style;
    }

    private static void setThinBorder(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }

    private static void setMediumBorder(CellStyle style) {
        style.setBorderTop(BorderStyle.MEDIUM);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setBorderLeft(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.MEDIUM);
    }
}
