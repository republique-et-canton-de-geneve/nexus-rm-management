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

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PermissionService {

    @Resource
    private NexusAccessService nexusAccessService;

    @Value("${app.security.token-base64}")
    private String token;

    /**
     * Displays the permissions of a user.
     */
    public void showUserPermissions(String userId) {
        List<User> users = nexusAccessService.getUser(userId);

        if (users.isEmpty()) {
            log.warn("User [{}] not found", userId);
        } else {
            var user = users.getFirst();
            log.info(user.toString());
            writePermissionsToExcel(user);
        }
    }

    public void showEmbeddedRoles() {
        getEmbeddedRolesByRole().entrySet().stream()
                .map(e -> {
                        var roleName = e.getKey().getName();
                        var embeddedRoles = e.getValue();
                        var embeddedRoleNames = embeddedRoles.stream()
                                       .map(Role::getName)
                                       .filter(s -> ! s.equals(roleName))
                                       .toList();
                        return e.getKey().getName() + ": " + embeddedRoleNames;
                })
                .sorted()
                .forEach(s -> log.info("{}", s));
    }

    public void showUsersHavingRole(String searchRoleName) {
        Set<String> usersWithRole = new HashSet<>();
        Map<String, Set<String>> usersByRole = showUsersHavingRole();
        usersByRole.forEach((roleName, roleUsers) -> {
            if (roleName.startsWith(searchRoleName)) {
                usersWithRole.addAll(roleUsers);
            }
        });

        log.info("Users found for role name [{}]: {}", searchRoleName, usersWithRole);
    }

    public void showRolesHavingPrivilege(String searchPrivilegeName) {
        var roles = nexusAccessService.getRoles().stream()
                .filter(role -> role.getPrivileges().stream()
                        .anyMatch(privilegeName -> privilegeName.startsWith(searchPrivilegeName)))
                .map(Role::getId)
                .sorted()
                .toList();

        log.info("Roles having privilege [{}]: {}", searchPrivilegeName, roles);
    }

    /**
     * Returns a Map containing all users by role.
     * Accounts for the fact that a role can have subroles (recursively) and external roles (also recursively).
     * Only the roles having at least one user are returned.
     * @return a Map where:
     *   A key is a role.
     *   A value is a list of user names
     */
    private Map<String, Set<String>> showUsersHavingRole() {
        log.info("Getting users by role");
        var usersToRoles = new HashMap<String, Set<String>>();

        var users = nexusAccessService.getUsers();
        log.info("Total number of found users = {}. Not all users are found, see the README", users.size());
        users.sort(Comparator.comparing(User::getUserId));

        nexusAccessService.getUsers()    // ATTENTION : NE TROUVE PAS TOUS LES USERS
                .stream()
                .sorted(Comparator.comparing(User::getUserId))
                .peek(user -> log.info("   Processing user {}", user.getUserId()))
                .forEach(user -> {
                    // direct roles
                    var rolesOfUser = user.getRoles().stream()
                            .flatMap(roleName -> getRoles(roleName).stream())
                            .map(Role::getName)
                            .collect(Collectors.toSet());
                        log.debug("Direct roles of {}: {}", user.getUserId(), rolesOfUser);
                    usersToRoles.put(user.getUserId(), rolesOfUser);

                    // external roles
                    var externalRolesOfUser = user.getExternalRoles().stream()
                            .flatMap(roleName -> getRoles(roleName).stream())
                            .map(Role::getName)
                            .collect(Collectors.toSet());
                        log.debug("External roles of {}: {}", user.getUserId(), externalRolesOfUser);

                    // les deux
                    rolesOfUser.addAll(externalRolesOfUser);
                    usersToRoles.put(user.getUserId(), rolesOfUser);
                });

        // invert the map : from <user, roles> to <role, users>
        var ret = new HashMap<String, Set<String>>();
        usersToRoles
                .forEach((user, userRoles) ->
                        userRoles.stream()
                                .forEach(roleName -> {
                                    if (ret.get(roleName) == null) {
                                        ret.put(roleName, new HashSet<>(Arrays.asList(user)));
                                    } else {
                                        ret.get(roleName).add(user);
                                    }
                                })
                );
        return ret;
    }

    /**
     * For every role, gets the roles embedded by that role.
     * @return a Map where:
     *   A key is a role (it cannot be the name of the role, because there are duplicated role names).
     *   A value is the list of the roles embedded by that role; this includes: the role itself, the
     *        roles of the role (recursively) and the external roles of the role (recursively)
     */
    private Map<Role, List<Role>> getEmbeddedRolesByRole() {
        var allRoles = nexusAccessService.getRoles();
        return allRoles.stream()
                .collect(Collectors.toMap(role -> role, role -> getRoles(role.getName())));
    }

    /**
     * Returns a list containing the specified role, its subroles (recursively) and its external roles (also
     * recursively.
     */
    private List<Role> getRoles(String roleName) {
        var ret = new ArrayList<Role>();

        // the role itself
        var optRole = nexusAccessService.getRole(roleName, false);
        if (optRole.isEmpty()) {
            return Collections.emptyList();
        }
        var role = optRole.get();
        ret.add(role);

        // its subroles
        if (role.getRoles() != null) {
            role.getRoles().stream()
                    .map(name -> nexusAccessService.getRole(name, false).get())
                    .flatMap(rol -> getRoles(rol.getName()).stream())
                    .forEach(ret::add);
        }

        // its external roles
        if (role.getExternalRoles() != null) {
            role.getExternalRoles().stream()
                    .map(name -> nexusAccessService.getRole(name, false).get())
                    .flatMap(rol -> getRoles(rol.getName()).stream())
                    .forEach(ret::add);
        }

        return ret;
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

        saveWorkbook(workbook, user);
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
                if (privilege.isEmpty()) {
                    log.warn("Privilege [{}] is empty", privilegeName);
                    continue;
                }

                List<String> actions = privilege.get().getActions();
                String contentSelectorName = privilege.get().getContentSelector();  // can be null, for example for the built-in privilege "nx-apikey-all"

                String expression = "";
                if (contentSelectorName != null) {
                    expression = nexusAccessService.getContentSelector(contentSelectorName)
                        .map(ContentSelector::getExpression)
                        .orElse("");
                }

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

    private void saveWorkbook(Workbook workbook, User user) {
        try {
            File outputDir = new File("output");
            if (!outputDir.exists()) {
                outputDir.mkdir();
            }
            long timestamp = System.currentTimeMillis() / 1000L; // Get current time in Unix format (seconds)
            String fileName = "permissions_" + user.getUserId() + "_" + timestamp + ".xlsx";
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
