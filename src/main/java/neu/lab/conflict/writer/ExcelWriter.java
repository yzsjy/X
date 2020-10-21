package neu.lab.conflict.writer;

import neu.lab.conflict.vo.ExcelDataVO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class ExcelWriter {
    private static List<String> CELL_HEADS;

    static {
        CELL_HEADS = new ArrayList<>();
        CELL_HEADS.add("Projects");
        CELL_HEADS.add("# Star");
        CELL_HEADS.add("# Dependencies");
        CELL_HEADS.add("The average number of releases of the projects' dependencies");
        CELL_HEADS.add("GroupId");
        CELL_HEADS.add("ArtifactId");
        CELL_HEADS.add("Revised version");
        CELL_HEADS.add("Original version");
        CELL_HEADS.add("Conflict info");
        CELL_HEADS.add("Conflict versions");
        CELL_HEADS.add("File path");
    }

    public static Workbook exportData(ExcelDataVO data) {
        Workbook workbook = new SXSSFWorkbook();
        Sheet sheet = buildDataSheet(workbook);
        Row row = sheet.createRow(1);
        convertDataToRaw(data, row);
        return workbook;
    }

    public static void insertData(ExcelDataVO data, Workbook workBook) {
        Sheet sheet = workBook.getSheetAt(0);
        int line = sheet.getPhysicalNumberOfRows();
        Row row = sheet.createRow((short)line);
        convertDataToRaw(data, row);
    }

    public static Workbook getWorkBook(InputStream inputStream) {
        Workbook workbook = null;
        try {
            workbook = new XSSFWorkbook(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return workbook;
    }

    public static Sheet buildDataSheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet();
        for (int i = 0; i < CELL_HEADS.size(); i++) {
            sheet.setColumnWidth(i, 4000);
        }
        sheet.setDefaultRowHeight((short) 400);
        CellStyle cellStyle = buildHeadsCellStyle(sheet.getWorkbook());
        Row head = sheet.createRow(0);
        for (int i = 0; i < CELL_HEADS.size(); i++) {
            Cell cell = head.createCell(i);
            cell.setCellValue(CELL_HEADS.get(i));
            cell.setCellStyle(cellStyle);
        }
        return sheet;
    }

    public static CellStyle buildHeadsCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        //对齐方式设置
        style.setAlignment(HorizontalAlignment.CENTER);
        //边框颜色和宽度设置
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex()); // 下边框
        style.setBorderLeft(BorderStyle.THIN);
        style.setLeftBorderColor(IndexedColors.BLACK.getIndex()); // 左边框
        style.setBorderRight(BorderStyle.THIN);
        style.setRightBorderColor(IndexedColors.BLACK.getIndex()); // 右边框
        style.setBorderTop(BorderStyle.THIN);
        style.setTopBorderColor(IndexedColors.BLACK.getIndex()); // 上边框
        //设置背景颜色
        style.setFillForegroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        //粗体字设置
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    public static void convertDataToRaw(ExcelDataVO data, Row row) {
        int cellNum = 0;
        Cell cell;
        cell = row.createCell(cellNum++);
        cell.setCellValue(data.getProjectName());
        cell = row.createCell(cellNum++);
        cell.setCellValue(data.getStars());
        cell = row.createCell(cellNum++);
        cell.setCellValue(data.getDepNum());
        cell = row.createCell(cellNum++);
        cell.setCellValue(data.getAvgNum());
        cell = row.createCell(cellNum++);
        cell.setCellValue(data.getGroupId());
        cell = row.createCell(cellNum++);
        cell.setCellValue(data.getArtifactId());
        cell = row.createCell(cellNum++);
        cell.setCellValue(data.getChangeVersion());
        cell = row.createCell(cellNum++);
        cell.setCellValue(data.getOriginalVersion());
        cell = row.createCell(cellNum++);
        cell.setCellValue(data.getConflictInfo());
        cell = row.createCell(cellNum++);
        cell.setCellValue(data.getConflictVersions());
        cell = row.createCell(cellNum++);
        cell.setCellValue(data.getFilePath());
    }
}
