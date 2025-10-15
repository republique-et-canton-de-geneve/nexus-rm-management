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

import ch.ge.cti.nexus.nexusrmgui.business.certificate.Certificate;
import ch.ge.cti.nexus.nexusrmgui.business.NexusAccessService;
import ch.ge.cti.nexus.nexusrmgui.exception.ApplicationException;
import ch.ge.cti.nexus.nexusrmgui.util.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.IntStream;

import static ch.ge.cti.nexus.nexusrmgui.util.DateUtils.FORMATED_DATE;

@Service
@Slf4j
public class CertificateService {

    @Resource
    private NexusAccessService nexusAccessService;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    public List<Certificate> getExpiredCertificates(int days) {
        try {
            Certificate[] certificates = nexusAccessService.getCertificats();

            // Filter and sort certificates
            return Arrays.stream(certificates)
                    .filter(cert -> cert.getExpiresOn() <= System.currentTimeMillis() + days * 24 * 60 * 60 * 1000L) // Filter to get certificates that are expired or will expire in a week or less
                    .sorted((cert1, cert2) -> Long.compare(cert2.getExpiresOn(), cert1.getExpiresOn())) // Sort from most recent to oldest
                    .toList();

        } catch (RuntimeException e) {
            log.error("Error retrieving certificates", e);
            throw new ApplicationException(e);
        }
    }

    public void showExpiredCertificates(int days) {
        List<Certificate> expiredCertificates = getExpiredCertificates(days);
        writeCertificatesToExcel(expiredCertificates);
    }

    private void writeCertificatesToExcel(List<Certificate> certificates) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Certificates");

        // Create styles
        CellStyle redStyle = createCellStyle(workbook, IndexedColors.RED);
        CellStyle orangeStyle = createCellStyle(workbook, IndexedColors.ORANGE);
        CellStyle yellowStyle = createCellStyle(workbook, IndexedColors.YELLOW);
        CellStyle titleStyle = createTitleCellStyle(workbook);
        CellStyle lightGreyStyle = createCellStyle(workbook, IndexedColors.GREY_25_PERCENT);
        CellStyle whiteStyle = createCellStyle(workbook, IndexedColors.WHITE);

        // Write the header row
        writeHeaderRow(sheet, titleStyle);

        int rowNum = 1; // row 0 is the header
        for (Certificate cert : certificates) {
            CellStyle rowStyle = (rowNum % 2 == 0) ? lightGreyStyle : whiteStyle;
            rowNum = writeCertificateRow(sheet, cert, rowNum, redStyle, orangeStyle, yellowStyle, rowStyle);
        }

        // Auto-size columns
        autoSizeColumns(sheet);

        saveWorkbook(workbook);
    }

    private CellStyle createCellStyle(Workbook workbook, IndexedColors color) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(color.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createTitleCellStyle(Workbook workbook) {
        CellStyle style = createCellStyle(workbook, IndexedColors.GREY_50_PERCENT);
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        return style;
    }

    private void writeHeaderRow(Sheet sheet, CellStyle titleStyle) {
        Row headerRow = sheet.createRow(0);
        String[] titles = {
                "expiresOn", "issuedOn", "subjectCommonName", "subjectOrganization", "fingerprint", "id",
                "issuerCommonName", "issuerOrganization", "issuerOrganizationalUnit",
                "pem", "serialNumber"
        };

        IntStream.range(0, titles.length).forEach(i -> {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(titles[i]);
            cell.setCellStyle(titleStyle);
        });
    }

    private int writeCertificateRow(Sheet sheet, Certificate cert, int rowNum, CellStyle redStyle, CellStyle orangeStyle, CellStyle yellowStyle, CellStyle rowStyle) {
        Row row = sheet.createRow(rowNum);
        CellStyle expiresOnStyle = getExpiresOnStyle(cert.getExpiresOn(), redStyle, orangeStyle, yellowStyle);

        createCell(row, 0, formatDate(cert.getExpiresOn()), expiresOnStyle);
        createCell(row, 1, formatDate(cert.getIssuedOn()), rowStyle);
        createCell(row, 2, cert.getSubjectCommonName(), rowStyle);
        createCell(row, 3, cert.getSubjectOrganization(), rowStyle);
        createCell(row, 4, cert.getId(), rowStyle);
        createCell(row, 5, cert.getFingerprint(), rowStyle);
        createCell(row, 6, cert.getIssuerCommonName(), rowStyle);
        createCell(row, 7, cert.getIssuerOrganization(), rowStyle);
        createCell(row, 8, cert.getIssuerOrganizationalUnit(), rowStyle);
        createCell(row, 9, cert.getPem(), rowStyle);
        createCell(row, 10, cert.getSerialNumber(), rowStyle);

        return rowNum + 1;
    }

    private void createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        if (style != null) {
            cell.setCellStyle(style);
        }
    }

    private String formatDate(long timestamp) {
        return dateFormat.format(new Date(timestamp));
    }

    private void autoSizeColumns(Sheet sheet) {
        for (int i = 0; i < 11; i++) { // Assuming there are 11 columns
            sheet.autoSizeColumn(i);
        }
    }

    private void saveWorkbook(Workbook workbook) {
        try {
            File outputDir = new File("output");
            if (!outputDir.exists()) {
                outputDir.mkdir();
            }
            var fileName = "expired_certificates_" + FORMATED_DATE + ".xlsx";
            FileOutputStream fileOut = new FileOutputStream(new File(outputDir, fileName));
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();
            log.info("File [{}] successfully generated in directory [{}]", fileName, outputDir);
        } catch (IOException e) {
            log.error("Error writing Excel file", e);
        }
    }

    private CellStyle getExpiresOnStyle(long expiresOn, CellStyle redStyle, CellStyle orangeStyle, CellStyle yellowStyle) {
        long currentTime = System.currentTimeMillis();
        long diff = expiresOn - currentTime;
        long weekInMillis = 604800000; // résultat de 7 * 24 * 60 * 60 * 1000
        long fourDaysInMillis = 345600000; // résultat de 4 * 24 * 60 * 60 * 1000

        if (expiresOn <= currentTime) {
            return redStyle; // Already expired
        } else if (diff <= fourDaysInMillis) {
            return orangeStyle; // Will expire in 4 days or less
        } else if (diff <= weekInMillis) {
            return yellowStyle; // Will expire in 7-4 days
        } else {
            return null;
        }
    }
}
