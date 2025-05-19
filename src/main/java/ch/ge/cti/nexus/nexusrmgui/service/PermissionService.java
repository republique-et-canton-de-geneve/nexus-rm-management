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
import ch.ge.cti.nexus.nexusrmgui.business.permission.ContentSelectorResponse;
import ch.ge.cti.nexus.nexusrmgui.business.permission.User;
import ch.ge.cti.nexus.nexusrmgui.business.permission.PrivilegeResponse;
import ch.ge.cti.nexus.nexusrmgui.business.permission.RoleResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

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

    public void showPermissions(String userId) {
        List<User> users = nexusAccessService.getUsers().stream()
                .filter(role -> role.getUserId().equalsIgnoreCase(userId))
                .toList();

        if (users.isEmpty()) {
            log.info("User [{}] not found", userId);
        } else {
            for (User user : users) {
                log.info(user.toString());
            }
            writePermissionsToExcel(users);
        }
    }

    private void writePermissionsToExcel(List<User> permissions) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Permissions");

        // Create styles
        CellStyle titleStyle = createTitleCellStyle(workbook);
        CellStyle lightGreyStyle = createCellStyle(workbook, IndexedColors.GREY_25_PERCENT);
        CellStyle whiteStyle = createCellStyle(workbook, IndexedColors.WHITE);

        // Write the header row
        writeHeaderRow(sheet, titleStyle);

        int rowNum = 1; // row 0 is the header
        for (User user : permissions) {
            rowNum = writePermissionRows(sheet, user, rowNum, lightGreyStyle, whiteStyle);
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

    private int writeRoleWithSubRoles(Sheet sheet, String userId, String role, String parentRole, String parentExternalRole, int rowNum, CellStyle lightGreyStyle, CellStyle whiteStyle, Set<String> processedRoles) {
        if (processedRoles.contains(role)) {
            return rowNum;
        }
        processedRoles.add(role);

        List<String> privileges = getPrivilegesForRole(role);
        List<String> subRoles = getSubRolesForRole(role);

        if (privileges.isEmpty()) {
            CellStyle rowStyle = (rowNum % 2 == 0) ? lightGreyStyle : whiteStyle;
            Row row = sheet.createRow(rowNum++);
            createCell(row, 0, userId, rowStyle);
            createCell(row, 1, parentRole.isEmpty() ? role : parentRole, rowStyle);
            createCell(row, 2, parentRole.isEmpty() ? "" : role, rowStyle);
            createCell(row, 3, "", rowStyle);
            createCell(row, 4, "", rowStyle);
            createCell(row, 5, "", rowStyle);
            createCell(row, 6, "", rowStyle);
            createCell(row, 7, "", rowStyle);
            createCell(row, 8, "", rowStyle);
        } else {
            for (String privilege : privileges) {
                List<String> actions = getActionsForPrivilege(privilege);
                String contentSelector = getContentSelectorForPrivilege(privilege);
                String expression = contentSelector.isEmpty() ? "" : getExpressionForContentSelector(contentSelector);

                if (actions.isEmpty()) {
                    CellStyle rowStyle = (rowNum % 2 == 0) ? lightGreyStyle : whiteStyle;
                    Row row = sheet.createRow(rowNum++);
                    createCell(row, 0, userId, rowStyle);
                    createCell(row, 1, parentRole.isEmpty() ? role : parentRole, rowStyle);
                    createCell(row, 2, parentRole.isEmpty() ? "" : role, rowStyle);
                    createCell(row, 3, "", rowStyle);
                    createCell(row, 4, "", rowStyle);
                    createCell(row, 5, privilege, rowStyle);
                    createCell(row, 6, "", rowStyle);
                    createCell(row, 7, contentSelector, rowStyle);
                    createCell(row, 8, expression, rowStyle);
                } else {
                    for (String action : actions) {
                        CellStyle rowStyle = (rowNum % 2 == 0) ? lightGreyStyle : whiteStyle;
                        Row row = sheet.createRow(rowNum++);
                        createCell(row, 0, userId, rowStyle);
                        createCell(row, 1, parentRole.isEmpty() ? role : parentRole, rowStyle);
                        createCell(row, 2, parentRole.isEmpty() ? "" : role, rowStyle);
                        createCell(row, 3, "", rowStyle);
                        createCell(row, 4, "", rowStyle);
                        createCell(row, 5, privilege, rowStyle);
                        createCell(row, 6, action, rowStyle);
                        createCell(row, 7, contentSelector, rowStyle);
                        createCell(row, 8, expression, rowStyle);
                    }
                }
            }
        }

        for (String subRole : subRoles) {
            rowNum = writeRoleWithSubRoles(sheet, userId, subRole, role, parentExternalRole, rowNum, lightGreyStyle, whiteStyle, processedRoles);
        }

        return rowNum;
    }

    private int writeExternalRoleWithSubRoles(Sheet sheet, String userId, String externalRole, String parentExternalRole, String parentRole, int rowNum, CellStyle lightGreyStyle, CellStyle whiteStyle, Set<String> processedExternalRoles) {
        if (processedExternalRoles.contains(externalRole)) {
            return rowNum;
        }
        processedExternalRoles.add(externalRole);

        List<String> privileges = getPrivilegesForRole(externalRole);
        List<String> subExternalRoles = getSubRolesForRole(externalRole);

        if (privileges.isEmpty()) {
            CellStyle rowStyle = (rowNum % 2 == 0) ? lightGreyStyle : whiteStyle;
            Row row = sheet.createRow(rowNum++);
            createCell(row, 0, userId, rowStyle);
            createCell(row, 1, "", rowStyle);
            createCell(row, 2, "", rowStyle);
            createCell(row, 3, parentExternalRole.isEmpty() ? externalRole : parentExternalRole, rowStyle);
            createCell(row, 4, parentExternalRole.isEmpty() ? "" : externalRole, rowStyle);
            createCell(row, 5, "", rowStyle);
            createCell(row, 6, "", rowStyle);
            createCell(row, 7, "", rowStyle);
            createCell(row, 8, "", rowStyle);
        } else {
            for (String privilege : privileges) {
                List<String> actions = getActionsForPrivilege(privilege);
                String contentSelector = getContentSelectorForPrivilege(privilege);
                String expression = contentSelector.isEmpty() ? "" : getExpressionForContentSelector(contentSelector);

                if (actions.isEmpty()) {
                    CellStyle rowStyle = (rowNum % 2 == 0) ? lightGreyStyle : whiteStyle;
                    Row row = sheet.createRow(rowNum++);
                    createCell(row, 0, userId, rowStyle);
                    createCell(row, 1, "", rowStyle);
                    createCell(row, 2, "", rowStyle);
                    createCell(row, 3, parentExternalRole.isEmpty() ? externalRole : parentExternalRole, rowStyle);
                    createCell(row, 4, parentExternalRole.isEmpty() ? "" : externalRole, rowStyle);
                    createCell(row, 5, privilege, rowStyle);
                    createCell(row, 6, "", rowStyle);
                    createCell(row, 7, contentSelector, rowStyle);
                    createCell(row, 8, expression, rowStyle);
                } else {
                    for (String action : actions) {
                        CellStyle rowStyle = (rowNum % 2 == 0) ? lightGreyStyle : whiteStyle;
                        Row row = sheet.createRow(rowNum++);
                        createCell(row, 0, userId, rowStyle);
                        createCell(row, 1, "", rowStyle);
                        createCell(row, 2, "", rowStyle);
                        createCell(row, 3, parentExternalRole.isEmpty() ? externalRole : parentExternalRole, rowStyle);
                        createCell(row, 4, parentExternalRole.isEmpty() ? "" : externalRole, rowStyle);
                        createCell(row, 5, privilege, rowStyle);
                        createCell(row, 6, action, rowStyle);
                        createCell(row, 7, contentSelector, rowStyle);
                        createCell(row, 8, expression, rowStyle);
                    }
                }
            }
        }

        for (String subExternalRole : subExternalRoles) {
            rowNum = writeExternalRoleWithSubRoles(sheet, userId, subExternalRole, externalRole, parentRole, rowNum, lightGreyStyle, whiteStyle, processedExternalRoles);
        }

        return rowNum;
    }

    private List<String> getPrivilegesForRole(String role) {
        try {
            String uri = "/v1/security/roles/" + role;
            RoleResponse response = webClientProvider.getWebClient()
                    .get()
                    .uri(uri)
                    .accept(APPLICATION_JSON)
                    .header("Authorization", "Basic " + token)
                    .retrieve()
                    .bodyToMono(RoleResponse.class)
                    .block();
            if (response != null && response.getPrivileges() != null) {
                return response.getPrivileges();
            }
        } catch (WebClientResponseException.NotFound e) {
            log.warn("Role not found: " + role);
        } catch (RuntimeException e) {
            log.error("Error fetching privileges for role: " + role, e);
        }
        return List.of(); // Return an empty list if there was an error or no privileges
    }

    private List<String> getSubRolesForRole(String role) {
        try {
            String uri = "/v1/security/roles/" + role;
            RoleResponse response = webClientProvider.getWebClient()
                    .get()
                    .uri(uri)
                    .accept(APPLICATION_JSON)
                    .header("Authorization", "Basic " + token)
                    .retrieve()
                    .bodyToMono(RoleResponse.class)
                    .block();
            if (response != null && response.getRoles() != null) {
                return response.getRoles();
            }
        } catch (WebClientResponseException.NotFound e) {
            log.warn("Role not found: " + role);
        } catch (RuntimeException e) {
            log.error("Error fetching sub-roles for role: " + role, e);
        }
        return List.of(); // Return an empty list if there was an error or no sub-roles
    }

    private String getContentSelectorForPrivilege(String privilege) {
        try {
            String uri = "/v1/security/privileges/" + privilege;
            PrivilegeResponse response = webClientProvider.getWebClient()
                    .get()
                    .uri(uri)
                    .accept(APPLICATION_JSON)
                    .header("Authorization", "Basic " + token)
                    .retrieve()
                    .bodyToMono(PrivilegeResponse.class)
                    .block();
            if (response != null && response.getContentSelector() != null) {
                return response.getContentSelector();
            }
        } catch (WebClientResponseException.NotFound e) {
            log.warn("Privilege not found: " + privilege);
        } catch (RuntimeException e) {
            log.error("Error fetching content selector for privilege: " + privilege, e);
        }
        return ""; // Return an empty string if there was an error or no content selector
    }

    private String getExpressionForContentSelector(String contentSelector) {
        try {
            String uri = "/v1/security/content-selectors/" + contentSelector;
            ContentSelectorResponse response = webClientProvider.getWebClient()
                    .get()
                    .uri(uri)
                    .accept(APPLICATION_JSON)
                    .header("Authorization", "Basic " + token)
                    .retrieve()
                    .bodyToMono(ContentSelectorResponse.class)
                    .block();
            if (response != null && response.getExpression() != null) {
                return response.getExpression();
            }
        } catch (WebClientResponseException.NotFound e) {
            log.warn("Content selector not found: " + contentSelector);
        } catch (RuntimeException e) {
            log.error("Error fetching expression for content selector: " + contentSelector, e);
        }
        return ""; // Return an empty string if there was an error or no expression
    }

    private List<String> getActionsForPrivilege(String privilege) {
        try {
            String uri = "/v1/security/privileges/" + privilege;
            PrivilegeResponse response = webClientProvider.getWebClient()
                    .get()
                    .uri(uri)
                    .accept(APPLICATION_JSON)
                    .header("Authorization", "Basic " + token)
                    .retrieve()
                    .bodyToMono(PrivilegeResponse.class)
                    .block();
            if (response != null && response.getActions() != null) {
                return response.getActions();
            }
        } catch (WebClientResponseException.NotFound e) {
            log.warn("Privilege not found: " + privilege);
        } catch (RuntimeException e) {
            log.error("Error fetching actions for privilege: " + privilege, e);
        }
        return List.of(); // Return an empty list if there was an error or no actions
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
