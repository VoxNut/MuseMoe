package com.javaweb.utils;

import com.javaweb.constant.AppConstant;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.awt.Color;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class ExportFileUtil {
    public static <T extends Number> void createSheet(XSSFWorkbook workbook, String sheetName, String title, String[] headers, Map<String, T> data, LocalDate startDate, LocalDate endDate) {
        Sheet sheet = workbook.createSheet(sheetName);
        sheet.setDefaultRowHeight((short) 400);

        // Load custom font
        XSSFFont customFont = workbook.createFont();
        customFont.setFontName("JetBrains Mono Medium");
        customFont.setFontHeightInPoints((short) 12);
        customFont.setColor(new XSSFColor(AppConstant.TEXT_COLOR, null));

        // Create base style with background and text color
        XSSFCellStyle baseStyle = workbook.createCellStyle();
        baseStyle.setFillForegroundColor(new XSSFColor(AppConstant.BACKGROUND_COLOR, null));
        baseStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        baseStyle.setFont(customFont);
        setBorderStyle(baseStyle, AppConstant.TEXT_COLOR);

        // Header style
        XSSFCellStyle headerStyle = workbook.createCellStyle();
        headerStyle.cloneStyleFrom(baseStyle);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setBorderBottom(BorderStyle.MEDIUM);
        XSSFFont headerFont = workbook.createFont();
        headerFont.setFontName("JetBrains Mono Medium");
        headerFont.setFontHeightInPoints((short) 12);
        headerFont.setBold(true);
        headerFont.setColor(new XSSFColor(AppConstant.TEXT_COLOR, null));
        headerStyle.setFont(headerFont);
        setBorderStyle(headerStyle, AppConstant.TEXT_COLOR);

        // Title style without borders
        XSSFCellStyle titleStyle = workbook.createCellStyle();
        titleStyle.cloneStyleFrom(baseStyle);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        XSSFFont titleFont = workbook.createFont();
        titleFont.setFontName("JetBrains Mono Medium");
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 14);
        titleFont.setColor(new XSSFColor(AppConstant.TEXT_COLOR, null));
        titleStyle.setFont(titleFont);
        removeBorders(titleStyle);

        // Data style
        XSSFCellStyle dataStyle = workbook.createCellStyle();
        dataStyle.cloneStyleFrom(baseStyle);
        dataStyle.setAlignment(HorizontalAlignment.CENTER);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setFont(customFont);
        setBorderStyle(dataStyle, AppConstant.TEXT_COLOR);

        // Number style
        XSSFCellStyle numberStyle = workbook.createCellStyle();
        numberStyle.cloneStyleFrom(dataStyle);
        numberStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));
        setBorderStyle(numberStyle, AppConstant.TEXT_COLOR);

        // Time style without borders
        XSSFCellStyle timeStyle = workbook.createCellStyle();
        timeStyle.cloneStyleFrom(baseStyle);
        timeStyle.setFont(customFont);
        removeBorders(timeStyle);

        // Create title row
        Row titleRow = sheet.createRow(0);
        titleRow.setHeight((short) 800);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(title);
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));

        // Create header row
        Row headerRow = sheet.createRow(2);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Add data
        int rowNum = 3;
        long total = 0;
        int stt = 1;

        for (Map.Entry<String, T> entry : data.entrySet()) {
            Row row = sheet.createRow(rowNum++);

            Cell sttCell = row.createCell(0);
            sttCell.setCellValue(stt++);
            sttCell.setCellStyle(dataStyle);

            Cell typeCell = row.createCell(1);
            typeCell.setCellValue(entry.getKey());
            typeCell.setCellStyle(dataStyle);

            Cell quantityCell = row.createCell(2);
            quantityCell.setCellValue(entry.getValue().doubleValue());
            quantityCell.setCellStyle(numberStyle);

            total += entry.getValue().longValue();
        }

        // Add total row
        Row totalRow = sheet.createRow(rowNum);
        Cell totalLabelCell = totalRow.createCell(1);
        totalLabelCell.setCellValue("Tổng cộng:");
        totalLabelCell.setCellStyle(headerStyle);

        Cell totalValueCell = totalRow.createCell(2);
        totalValueCell.setCellValue(total);
        totalValueCell.setCellStyle(headerStyle);

        // Set column widths
        sheet.setColumnWidth(0, 3000);  // STT
        sheet.setColumnWidth(1, 10000); // Product Type
        sheet.setColumnWidth(2, 10000);  // Quantity

        // Add time generated
        Row timeRow = sheet.createRow(rowNum + 2);
        Cell timeCell = timeRow.createCell(0);
        timeCell.setCellValue("Thời gian xuất: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        timeCell.setCellStyle(timeStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum + 2, rowNum + 2, 0, 2));

        // Add startDate and endDate if they are not null
        if (startDate != null && endDate != null) {
            Row dateRow = sheet.createRow(rowNum + 3);
            Cell dateCell = dateRow.createCell(0);
            dateCell.setCellValue("Từ ngày: " + startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " đến ngày: " + endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            dateCell.setCellStyle(timeStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum + 3, rowNum + 3, 0, 2));
        }
    }

    public static void setBorderStyle(XSSFCellStyle style, Color color) {
        XSSFColor borderColor = new XSSFColor(color, null);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setTopBorderColor(borderColor);
        style.setBottomBorderColor(borderColor);
        style.setLeftBorderColor(borderColor);
        style.setRightBorderColor(borderColor);
    }

    public static void removeBorders(XSSFCellStyle style) {
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.NONE);
        style.setBorderLeft(BorderStyle.NONE);
        style.setBorderRight(BorderStyle.NONE);
    }
}