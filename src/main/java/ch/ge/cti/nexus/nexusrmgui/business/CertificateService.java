package ch.ge.cti.nexus.nexusrmgui.business;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CertificateService {

    @Resource
    private NexusAccessService nexusAccessService;

    public void montrerCertificatsEchus() {
        try {
            Certificate[] certificates = nexusAccessService.getCertificats();

            // Filter and sort certificates
            List<Certificate> relevantCertificates = Arrays.stream(certificates)
                    .filter(cert -> cert.getExpiresOn() <= System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L) // Filter to get certificates that are expired or will expire in a week or less
                    .sorted((cert1, cert2) -> Long.compare(cert2.getExpiresOn(), cert1.getExpiresOn())) // Sort from most recent to oldest
                    .toList();

            writeCertificatesToExcel(relevantCertificates);
        } catch (Exception e) {
            log.error("Error processing certificates", e);
        }
    }

    private void writeCertificatesToExcel(List<Certificate> certificates) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Certificates");

        // Set column widths
        setColumnWidths(sheet);

        // Create styles
        CellStyle redStyle = createCellStyle(workbook, IndexedColors.RED);
        CellStyle orangeStyle = createCellStyle(workbook, IndexedColors.ORANGE);
        CellStyle yellowStyle = createCellStyle(workbook, IndexedColors.YELLOW);
        CellStyle textStyle = createTextCellStyle(workbook);

        int rowNum = 0;
        for (Certificate cert : certificates) {
            rowNum = writeCertificate(sheet, cert, rowNum, redStyle, orangeStyle, yellowStyle, textStyle);
            rowNum++; // Add an empty line between certificates
        }

        saveWorkbook(workbook, "output", "certificats_échus");
    }

    private void setColumnWidths(Sheet sheet) {
        sheet.setColumnWidth(0, 25 * 256); // Column A
        sheet.setColumnWidth(1, 55 * 256); // Column B
    }

    private CellStyle createCellStyle(Workbook workbook, IndexedColors color) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(color.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createTextCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("@"));
        return style;
    }

    private int writeCertificate(Sheet sheet, Certificate cert, int startRow, CellStyle redStyle, CellStyle orangeStyle, CellStyle yellowStyle, CellStyle textStyle) {
        int rowNum = startRow;
        CellStyle expiresOnStyle = getExpiresOnStyle(cert.getExpiresOn(), redStyle, orangeStyle, yellowStyle);

        rowNum = createRow(sheet, rowNum, "expiresOn", String.valueOf(cert.getExpiresOn()), expiresOnStyle, textStyle);
        rowNum = createRow(sheet, rowNum, "fingerprint", cert.getFingerprint());
        rowNum = createRow(sheet, rowNum, "id", cert.getId());
        rowNum = createRow(sheet, rowNum, "issuedOn", String.valueOf(cert.getIssuedOn()), textStyle);
        rowNum = createRow(sheet, rowNum, "issuerCommonName", cert.getIssuerCommonName());
        rowNum = createRow(sheet, rowNum, "issuerOrganization", cert.getIssuerOrganization());
        rowNum = createRow(sheet, rowNum, "issuerOrganizationalUnit", cert.getIssuerOrganizationalUnit());
        rowNum = createRow(sheet, rowNum, "pem", cert.getPem());
        rowNum = createRow(sheet, rowNum, "serialNumber", cert.getSerialNumber());
        rowNum = createRow(sheet, rowNum, "subjectCommonName", cert.getSubjectCommonName());
        rowNum = createRow(sheet, rowNum, "subjectOrganization", cert.getSubjectOrganization());
        rowNum = createRow(sheet, rowNum, "subjectOrganizationalUnit", cert.getSubjectOrganizationalUnit());

        return rowNum;
    }

    private int createRow(Sheet sheet, int rowNum, String name, String value) {
        return createRow(sheet, rowNum, name, value, null, null);
    }

    private int createRow(Sheet sheet, int rowNum, String name, String value, CellStyle nameStyle) {
        return createRow(sheet, rowNum, name, value, nameStyle, null);
    }

    private int createRow(Sheet sheet, int rowNum, String name, String value, CellStyle nameStyle, CellStyle valueStyle) {
        Row row = sheet.createRow(rowNum++);
        Cell cell1 = row.createCell(0);
        cell1.setCellValue(name);
        if (nameStyle != null) cell1.setCellStyle(nameStyle);

        Cell cell2 = row.createCell(1);
        cell2.setCellValue(value);
        if (valueStyle != null) cell2.setCellStyle(valueStyle);

        return rowNum;
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

    private CellStyle getExpiresOnStyle(long expiresOn, CellStyle redStyle, CellStyle orangeStyle, CellStyle yellowStyle) {
        long currentTime = System.currentTimeMillis();
        long diff = expiresOn - currentTime;
        long weekInMillis = 7 * 24 * 60 * 60 * 1000;
        long fourDaysInMillis = 4 * 24 * 60 * 60 * 1000;

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
