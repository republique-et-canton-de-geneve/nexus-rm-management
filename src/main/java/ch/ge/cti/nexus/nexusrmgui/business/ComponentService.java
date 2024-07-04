package ch.ge.cti.nexus.nexusrmgui.business;

import lombok.extern.slf4j.Slf4j;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ComponentService {

    @Resource
    private NexusAccessService nexusAccessService;

    public void montrerComponents() {
        List<Component> components = nexusAccessService.getComponents();
        writeComponentsToExcel(components);
        for (Component component : components) {
            log.info(component.toString());
        }
    }

    private void writeComponentsToExcel(List<Component> components) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Components");

        // Set column widths
        setColumnWidths(sheet);

        // Create styles
        CellStyle boldStyle = createBoldStyle(workbook);
        CellStyle normalStyle = createNormalStyle(workbook);
        CellStyle fileSizeStyle = createFileSizeStyle(workbook);

        int rowNum = 0;
        Row headerRow = sheet.createRow(rowNum++);
        setRowHeight(headerRow);

        // Create headers
        createCell(headerRow, 0, "ID", boldStyle);
        createCell(headerRow, 1, "GROUP", boldStyle);
        createCell(headerRow, 2, "NAME", boldStyle);
        createCell(headerRow, 3, "VERSION", boldStyle);
        createCell(headerRow, 4, "FILESIZE", boldStyle);
        createCell(headerRow, 5, "LASTMODIFIED", boldStyle);
        createCell(headerRow, 6, "PATH", boldStyle);

        for (Component component : components) {
            boolean firstRow = true;

            for (Asset asset : component.getAssets()) {
                Row row = sheet.createRow(rowNum++);
                setRowHeight(row);

                createCell(row, 0, component.getId(), firstRow ? boldStyle : normalStyle);
                createCell(row, 1, component.getGroup(), firstRow ? boldStyle : normalStyle);
                createCell(row, 2, component.getName(), firstRow ? boldStyle : normalStyle);
                createCell(row, 3, component.getVersion(), firstRow ? boldStyle : normalStyle);

                createCell(row, 4, String.valueOf(asset.getFileSize()), fileSizeStyle);
                createCell(row, 5, asset.getLastModified(), normalStyle);
                createCell(row, 6, asset.getPath(), normalStyle);

                firstRow = false;
            }

            // Add an empty line between components
            // sheet.createRow(rowNum++);
        }

        saveWorkbook(workbook, "output", "components");
    }

    private void setColumnWidths(Sheet sheet) {
        sheet.setColumnWidth(0, 78 * 256); // ID column
        sheet.setColumnWidth(1, 28 * 256); // GROUP column
        sheet.setColumnWidth(2, 28 * 256); // NAME column
        sheet.setColumnWidth(3, 28 * 256); // VERSION column
        sheet.setColumnWidth(4, 28 * 256); // FILESIZE column
        sheet.setColumnWidth(5, 35 * 256); // LASTMODIFIED column
        sheet.setColumnWidth(6, 85 * 256); // PATH column
    }

    private void setRowHeight(Row row) {
        row.setHeightInPoints(23); // Set the height to 23 points
    }

    private CellStyle createBoldStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setWrapText(true);
        return style;
    }

    private CellStyle createNormalStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setWrapText(true);
        return style;
    }

    private CellStyle createFileSizeStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setWrapText(true);
        return style;
    }

    private void createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void saveWorkbook(Workbook workbook, String outputDirName, String baseFileName) {
        try {
            File outputDir = new File(outputDirName);
            if (!outputDir.exists()) {
                outputDir.mkdir();
            }
            long timestamp = System.currentTimeMillis() / 1000L; // Get current time in Unix format (seconds)
            String fileName = baseFileName + "_" + timestamp + ".xlsx";
            FileOutputStream fileOut = new FileOutputStream(new File(outputDir, fileName));
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();
            log.info("Excel file has been generated successfully.");
        } catch (IOException e) {
            log.error("Error writing Excel file", e);
        }
    }
}
