/*
 * Copyright (C) <Date> Republique et canton de Geneve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.ge.cti.nexus.nexusrmgui.service;

import ch.ge.cti.nexus.nexusrmgui.business.component.Asset;
import ch.ge.cti.nexus.nexusrmgui.business.component.Component;
import ch.ge.cti.nexus.nexusrmgui.business.component.ComponentResponse;
import ch.ge.cti.nexus.nexusrmgui.business.NexusAccessService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ComponentService {

    @Resource
    private NexusAccessService nexusAccessService;

    @Value("${app.business.component.minimum-size-file}")
    private int minSize;

    @Value("${app.business.component.latest}")
    private int latestComponent;

    public void showComponents() {
        List<Component> components = fetchAllComponents();
        writeComponentsToExcel(components,"heavyComponents");
        for (Component component : components) {
            log.info(component.toString());
        }
    }

    public void deleteComponents(boolean dryRun) {
        List<Component> components = fetchAllComponents();

        components.sort(Comparator.comparing(component ->
                component.getAssets().stream()
                        .map(Asset::getLastModified)
                        .max(Comparator.naturalOrder())
                        .orElse(null)));


        List<Component> componentsToDelete = components.stream()
                .skip(latestComponent)
                .collect(Collectors.toList());

        writeComponentsToExcel(componentsToDelete, "deletedComponents");

        if (dryRun) {
            simulateDeletion(componentsToDelete);
        } else {
            deleteComponents(componentsToDelete);
        }
    }

    private void deleteComponents(List<Component> componentsToDelete) {
        componentsToDelete
                .forEach(component -> {
                    log.info("Deleting component: " + component.toString());
                    nexusAccessService.deleteComponent(component.getId());
                });
    }

    private void simulateDeletion(List<Component> componentsToDelete) {
        componentsToDelete
                .forEach(component -> log.info("DRY RUN - Would delete: " + component.toString()));
    }

    private List<Component> fetchAllComponents() {
        List<Component> allComponents = new ArrayList<>();
        String continuationToken = null;

        do {
            ComponentResponse response = nexusAccessService.getComponents(continuationToken);
            if (response != null && response.getItems() != null) {
                List<Component> components = response.getItems().stream()
                        .map(component -> {
                            List<Asset> sortedAssets = component.getAssets().stream()
                                    .filter(asset -> asset.getFileSize() >= minSize)
                                    .sorted(Comparator.comparingLong(Asset::getFileSize).reversed())
                                    .toList();
                            component.setAssets(sortedAssets);
                            return component;
                        })
                        .filter(component -> !component.getAssets().isEmpty())
                        .toList();

                allComponents.addAll(components);
                continuationToken = response.getContinuationToken();
            } else {
                continuationToken = null;
            }
        } while (continuationToken != null);

        allComponents.sort(Comparator.comparing(Component::getMaxAssetSize).reversed());

        return allComponents;
    }

    private void writeComponentsToExcel(List<Component> components, String baseFileName) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(baseFileName);

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
        createCell(headerRow, 0, "GROUP", boldStyle);
        createCell(headerRow, 1, "NAME", boldStyle);
        createCell(headerRow, 2, "VERSION", boldStyle);
        createCell(headerRow, 3, "FILESIZE", boldStyle);
        createCell(headerRow, 4, "LASTMODIFIED", boldStyle);
        createCell(headerRow, 5, "PATH", boldStyle);
        createCell(headerRow, 6, "ID", boldStyle);

        for (Component component : components) {
            boolean firstRow = true;

            for (Asset asset : component.getAssets()) {
                Row row = sheet.createRow(rowNum++);
                setRowHeight(row);

                createCell(row, 0, component.getGroup(), firstRow ? boldStyle : normalStyle);
                createCell(row, 1, component.getName(), firstRow ? boldStyle : normalStyle);
                createCell(row, 2, component.getVersion(), firstRow ? boldStyle : normalStyle);

                createCell(row, 3, String.valueOf(asset.getFileSize()), fileSizeStyle);
                createCell(row, 4, asset.getLastModified().toString(), normalStyle);
                createCell(row, 5, asset.getPath(), normalStyle);
                createCell(row, 6, component.getId(), firstRow ? boldStyle : normalStyle);

                firstRow = false;
            }
        }
        saveWorkbook(workbook, "output", "components");
    }


    private void setColumnWidths(Sheet sheet) {
        sheet.setColumnWidth(0, 28 * 256); // GROUP column
        sheet.setColumnWidth(1, 28 * 256); // NAME column
        sheet.setColumnWidth(2, 28 * 256); // VERSION column
        sheet.setColumnWidth(3, 28 * 256); // FILESIZE column
        sheet.setColumnWidth(4, 28 * 256); // LASTMODIFIED column
        sheet.setColumnWidth(5, 85 * 256); // PATH column
        sheet.setColumnWidth(6, 85 * 256); // ID column
    }

    private void setRowHeight(Row row) {
        row.setHeightInPoints(23);
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
            long timestamp = System.currentTimeMillis() / 1000L;
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
