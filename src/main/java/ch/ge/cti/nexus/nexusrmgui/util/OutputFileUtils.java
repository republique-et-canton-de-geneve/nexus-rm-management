package ch.ge.cti.nexus.nexusrmgui.util;

import ch.ge.cti.nexus.nexusrmgui.business.permission.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
public class OutputFileUtils {

    private OutputFileUtils() {
    }

    public static final String OUTPUT_DIRECTORY = "output";

    public static final String FORMATTED_DATE =
            new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

    public static void saveWorkbook(Workbook workbook, String fileNameBase) {
        try {
            File outputDir = new File(OUTPUT_DIRECTORY);
            if (!outputDir.exists()) {
                outputDir.mkdir();
            }
            String fileName = fileNameBase + "_" + FORMATTED_DATE + ".xlsx";
            FileOutputStream fileOut = new FileOutputStream(new File(outputDir, fileName));
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();
            log.info("Excel file {}/{} been generated successfully", OUTPUT_DIRECTORY, fileName);
        } catch (IOException e) {
            log.error("Error writing Excel file", e);
        }
    }

}
