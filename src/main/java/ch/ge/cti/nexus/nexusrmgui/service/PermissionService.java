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

import ch.ge.cti.nexus.nexusrmgui.WebClientProvider;
import ch.ge.cti.nexus.nexusrmgui.business.NexusAccessService;
import ch.ge.cti.nexus.nexusrmgui.business.permission.ContentSelector;
import ch.ge.cti.nexus.nexusrmgui.business.permission.Privilege;
import ch.ge.cti.nexus.nexusrmgui.business.permission.Role;
import ch.ge.cti.nexus.nexusrmgui.business.permission.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Service
@Slf4j
public class PermissionService {

    @Resource
    private NexusAccessService nexusAccessService;

    @Resource
    private WebClientProvider webClientProvider;

    @Value("${app.security.token-base64}")
    private String token;

    /**
     * Displays the permissions of a user.
     */
    public void showUserPermissions(String userId) {
        List<User> users = nexusAccessService.getUser(userId);

        if (users.isEmpty()) {
            log.info("User [{}] not found", userId);
        } else {
            var user = users.getFirst();
            log.info(user.toString());
            writePermissionsToExcel(user);
        }
    }

    private void writePermissionsToExcel(User user) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Permissions");

        // Create styles
        CellStyle titleStyle = createTitleCellStyle(workbook);
        CellStyle lightGreyStyle = createCellStyle(workbook, IndexedColors.GREY_25_PERCENT);
        CellStyle whiteStyle = createCellStyle(workbook, IndexedColors.WHITE);

        // Write the header row
        writeHeaderRow(sheet, titleStyle);

        int rowNum = 1; // row 0 is the header
        writePermissionRows(sheet, user, rowNum, lightGreyStyle, whiteStyle);

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
        String[] titles = {"User", "Role", "SubRole", "ExternalRole", "SubExternalRole", "Privilege", "Action", "ContentSelector", "Expression"};

        for (int i = 0; i < titles.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(titles[i].toUpperCase());
            cell.setCellStyle(titleStyle);
        }
    }

    private int writePermissionRows(Sheet sheet, User user, int rowNum, CellStyle lightGreyStyle, CellStyle whiteStyle) {
        String userId = user.getUserId().toUpperCase();

        List<String> roles = user.getRoles().stream()
                .map(String::toUpperCase)
                .filter(role -> !role.startsWith("NX"))
                .toList();

        List<String> externalRoles = user.getExternalRoles().stream()
                .map(String::toUpperCase)
                .toList();

        Set<String> processedRoles = new HashSet<>();
        Set<String> processedExternalRoles = new HashSet<>();

        // Write roles and privileges first
        for (String role : roles) {
            rowNum = writeRoleWithSubRoles(sheet, userId, role, "", "", rowNum, lightGreyStyle, whiteStyle, processedRoles);
        }

        // Write externalRoles and their privileges after roles
        for (String externalRole : externalRoles) {
            rowNum = writeExternalRoleWithSubRoles(sheet, userId, externalRole, "", "", rowNum, lightGreyStyle, whiteStyle, processedExternalRoles);
        }

        return rowNum;
    }

    private int writeRoleWithSubRoles(
            Sheet sheet,
            String userId,
            String roleId,
            String parentRole,
            String parentExternalRole,
            int rowNum,
            CellStyle lightGreyStyle,
            CellStyle whiteStyle,
            Set<String> processedRoles) {

        if (processedRoles.contains(roleId)) {
            return rowNum;
        }
        processedRoles.add(roleId);

        Optional<Role> role = nexusAccessService.getRole(roleId);

        List<String> privilegeNames = role.map(Role::getPrivileges).orElse(Collections.emptyList());
        if (privilegeNames.isEmpty()) {
            CellStyle rowStyle = (rowNum % 2 == 0) ? lightGreyStyle : whiteStyle;
            Row row = sheet.createRow(rowNum++);
            createCell(row, 0, userId, rowStyle);
            createCell(row, 1, parentRole.isEmpty() ? roleId : parentRole, rowStyle);
            createCell(row, 2, parentRole.isEmpty() ? "" : roleId, rowStyle);
            createCell(row, 3, "", rowStyle);
            createCell(row, 4, "", rowStyle);
            createCell(row, 5, "", rowStyle);
            createCell(row, 6, "", rowStyle);
            createCell(row, 7, "", rowStyle);
            createCell(row, 8, "", rowStyle);
        } else {
            for (String privilegeName : privilegeNames) {
                Optional<Privilege> privilege = nexusAccessService.getPrivilege(privilegeName);
                List<String> actions = privilege
                        .map(Privilege::getActions)
                        .orElse(Collections.emptyList());
                String contentSelectorName = privilege
                        .map(Privilege::getContentSelector)
                        .orElse("");

                String expression = nexusAccessService.getContentSelector(contentSelectorName)
                        .map(ContentSelector::getExpression)
                        .orElse("");

                if (actions.isEmpty()) {
                    CellStyle rowStyle = (rowNum % 2 == 0) ? lightGreyStyle : whiteStyle;
                    Row row = sheet.createRow(rowNum++);
                    createCell(row, 0, userId, rowStyle);
                    createCell(row, 1, parentRole.isEmpty() ? roleId : parentRole, rowStyle);
                    createCell(row, 2, parentRole.isEmpty() ? "" : roleId, rowStyle);
                    createCell(row, 3, "", rowStyle);
                    createCell(row, 4, "", rowStyle);
                    createCell(row, 5, privilegeName, rowStyle);
                    createCell(row, 6, "", rowStyle);
                    createCell(row, 7, contentSelectorName, rowStyle);
                    createCell(row, 8, expression, rowStyle);
                } else {
                    for (String action : actions) {
                        CellStyle rowStyle = (rowNum % 2 == 0) ? lightGreyStyle : whiteStyle;
                        Row row = sheet.createRow(rowNum++);
                        createCell(row, 0, userId, rowStyle);
                        createCell(row, 1, parentRole.isEmpty() ? roleId : parentRole, rowStyle);
                        createCell(row, 2, parentRole.isEmpty() ? "" : roleId, rowStyle);
                        createCell(row, 3, "", rowStyle);
                        createCell(row, 4, "", rowStyle);
                        createCell(row, 5, privilegeName, rowStyle);
                        createCell(row, 6, action, rowStyle);
                        createCell(row, 7, contentSelectorName, rowStyle);
                        createCell(row, 8, expression, rowStyle);
                    }
                }
            }
        }

        List<String> subRoles = role
                .map(Role::getRoles)
                .orElse(Collections.emptyList());
        for (String subRole : subRoles) {
            rowNum = writeRoleWithSubRoles(sheet, userId, subRole, roleId, parentExternalRole, rowNum, lightGreyStyle, whiteStyle, processedRoles);
        }

        return rowNum;
    }

    private int writeExternalRoleWithSubRoles(
            Sheet sheet,
            String userId,
            String externalRoleId,
            String parentExternalRole,
            String parentRole,
            int rowNum,
            CellStyle lightGreyStyle,
            CellStyle whiteStyle,
            Set<String> processedExternalRoles) {

        if (processedExternalRoles.contains(externalRoleId)) {
            return rowNum;
        }
        processedExternalRoles.add(externalRoleId);

        Optional<Role> role = nexusAccessService.getRole(externalRoleId);

        List<String> privilegeNames = role
                .map(Role::getPrivileges)
                .orElse(new ArrayList<>());

        if (privilegeNames.isEmpty()) {
            CellStyle rowStyle = (rowNum % 2 == 0) ? lightGreyStyle : whiteStyle;
            Row row = sheet.createRow(rowNum++);
            createCell(row, 0, userId, rowStyle);
            createCell(row, 1, "", rowStyle);
            createCell(row, 2, "", rowStyle);
            createCell(row, 3, parentExternalRole.isEmpty() ? externalRoleId : parentExternalRole, rowStyle);
            createCell(row, 4, parentExternalRole.isEmpty() ? "" : externalRoleId, rowStyle);
            createCell(row, 5, "", rowStyle);
            createCell(row, 6, "", rowStyle);
            createCell(row, 7, "", rowStyle);
            createCell(row, 8, "", rowStyle);
        } else {
            for (String privilegeName : privilegeNames) {
                Optional<Privilege> privilege = nexusAccessService.getPrivilege(privilegeName);
                List<String> actions = privilege
                        .map(Privilege::getActions)
                        .orElse(Collections.emptyList());
                String contentSelectorName = privilege
                        .map(Privilege::getContentSelector)
                        .orElse("");

                String expression = contentSelectorName.isEmpty() ? "" : nexusAccessService
                        .getContentSelector(contentSelectorName)
                        .map(ContentSelector::getExpression)
                        .orElse("");

                if (actions.isEmpty()) {
                    CellStyle rowStyle = (rowNum % 2 == 0) ? lightGreyStyle : whiteStyle;
                    Row row = sheet.createRow(rowNum++);
                    createCell(row, 0, userId, rowStyle);
                    createCell(row, 1, "", rowStyle);
                    createCell(row, 2, "", rowStyle);
                    createCell(row, 3, parentExternalRole.isEmpty() ? externalRoleId : parentExternalRole, rowStyle);
                    createCell(row, 4, parentExternalRole.isEmpty() ? "" : externalRoleId, rowStyle);
                    createCell(row, 5, privilegeName, rowStyle);
                    createCell(row, 6, "", rowStyle);
                    createCell(row, 7, contentSelectorName, rowStyle);
                    createCell(row, 8, expression, rowStyle);
                } else {
                    for (String action : actions) {
                        CellStyle rowStyle = (rowNum % 2 == 0) ? lightGreyStyle : whiteStyle;
                        Row row = sheet.createRow(rowNum++);
                        createCell(row, 0, userId, rowStyle);
                        createCell(row, 1, "", rowStyle);
                        createCell(row, 2, "", rowStyle);
                        createCell(row, 3, parentExternalRole.isEmpty() ? externalRoleId : parentExternalRole, rowStyle);
                        createCell(row, 4, parentExternalRole.isEmpty() ? "" : externalRoleId, rowStyle);
                        createCell(row, 5, privilegeName, rowStyle);
                        createCell(row, 6, action, rowStyle);
                        createCell(row, 7, contentSelectorName, rowStyle);
                        createCell(row, 8, expression, rowStyle);
                    }
                }
            }
        }

        List<String> subExternalRoles = role
                .map(Role::getRoles)
                .orElse(new ArrayList<>());
        for (String subExternalRole : subExternalRoles) {
            rowNum = writeExternalRoleWithSubRoles(sheet, userId, subExternalRole, externalRoleId, parentRole, rowNum, lightGreyStyle, whiteStyle, processedExternalRoles);
        }

        return rowNum;
    }

    private void createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        if (style != null) {
            cell.setCellStyle(style);
        }
    }

    private void autoSizeColumns(Sheet sheet) {
        for (int i = 0; i < 9; i++) { // Update to 9 columns
            sheet.autoSizeColumn(i);
        }
    }

    private void saveWorkbook(Workbook workbook) {
        try {
            File outputDir = new File("output");
            if (!outputDir.exists()) {
                outputDir.mkdir();
            }
            long timestamp = System.currentTimeMillis() / 1000L; // Get current time in Unix format (seconds)
            String fileName = "permissions_" + timestamp + ".xlsx";
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
