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
import ch.ge.cti.nexus.nexusrmgui.service.YdpService;
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

    @Resource
    private YdpService ydpService;

    int days;

    public static void main(String[] args) {

        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws InterruptedException {
        final int DAYS_IN_MONTH = 30;

        if (args.length > 0) {
            String option = args[0];

            switch (option) {
                case "certificates", "1":
                    if (args.length > 1) {
                        days = Integer.parseInt(args[1]);
                    } else {
                        days = DAYS_IN_MONTH;
                    }
                    certificateService.showExpiredCertificates(days);
                    break;
                case "heavyComponents", "2":
                    componentService.showComponents();
                    break;
                case "permissions", "3":
                    if (args.length > 1) {
                        var userId = args[1].toUpperCase(ENGLISH);
                        permissionService.showUserPermissions(userId);
                    } else {
                        System.out.println("Please provide a user ID for the 'permission' option.");
                    }
                    break;
                case "deleteComponents", "4":
                    var dryRun = true;
                    // By default, dryRun is true. It is set to false only if the second argument is "realRun".
                    if (args.length > 1 && args[1].equalsIgnoreCase("realRun")) {
                        dryRun = false;
                    }
                    componentService.deleteComponents(dryRun);
                    break;
                case "5":
                    ydpService.getSensitiveDockerPrivileges();
                    break;
                case "6":
                    ydpService.fixDockerPrivileges();
                    break;
                case "7":
                    var usersByRole = permissionService.getUsersByRole();
                    log.info("Users by role: {}", usersByRole);
                    break;
                default:
                    System.out.println("Invalid option. Use 'certificate' or '1' for certificates, 'heavyComponents' or '2' for heavyComponents, 'permissions' or '3' for permissions, 'deleteComponents' or '4' for deleteComponents.");
            }
        } else {
            System.out.println("No arguments provided. Use 'certificate' or '1' for certificates, 'heavyComponents' or '2' for heavyComponents, 'permissions' or '3' for permissions, 'deleteComponents' or '4' for deleteComponents. See the README file.");
        }
    }

}
