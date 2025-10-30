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

package ch.ge.cti.nexus.nexusrmgui;

import ch.ge.cti.nexus.nexusrmgui.service.CertificateService;
import ch.ge.cti.nexus.nexusrmgui.service.ComponentService;
import ch.ge.cti.nexus.nexusrmgui.service.PermissionService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static java.util.Locale.ENGLISH;

@SpringBootApplication
@Slf4j
public class Application implements CommandLineRunner {

    @Resource
    private CertificateService certificateService;

    @Resource
    private ComponentService componentService;

    @Resource
    private PermissionService permissionService;

    int days;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws InterruptedException {
        final int DAYS_IN_MONTH = 30;

        final var MESSAGE = "Use either '1' for expired certificates"
                + " or '2' for heavy components"
                + " or '3' for permissions"
                + " or '4' for deleting components"
                + " or '5' for embedded roles"
                + " or '6' for users by role"
                + ". See the README file.";

        if (args.length > 0) {
            String option = args[0];

            switch (option) {
                case "1":
                    // expired certificates
                    if (args.length > 1) {
                        days = Integer.parseInt(args[1]);
                    } else {
                        days = DAYS_IN_MONTH;
                    }
                    certificateService.showExpiredCertificates(days);
                    break;
                case "2":
                    // heavy components
                    componentService.showComponents();
                    break;
                case "3":
                    // permissions
                    if (args.length > 1) {
                        var userId = args[1].toUpperCase(ENGLISH);
                        permissionService.showUserPermissions(userId);
                    } else {
                        log.error("Option '1' requires a user ID as a second argument");
                    }
                    break;
                case "4":
                    // delete components
                    var dryRun = true;
                    // By default, dryRun is true. It is set to false only if the second argument is "realRun".
                    if (args.length > 1 && args[1].equalsIgnoreCase("realRun")) {
                        dryRun = false;
                    }
                    componentService.deleteComponents(dryRun);
                    break;
                case "5":
                    // embedded roles
                    permissionService.showEmbeddedRoles();
                    break;
                case "6":
                    // user having a specified role
                    var roleName = args[1];
                    permissionService.showUsersHavingRole(roleName);
                    break;
                case "7":
                    // roles having a specified privilege
                    var privilegeName = args[1];
                    permissionService.showRolesHavingPrivilege(privilegeName);
                    break;
                default:
                    log.error("Invalid option. " + MESSAGE);
            }
        } else {
            log.error("No arguments provided. " + MESSAGE);
        }
    }

}
