package ch.ge.cti.nexus.nexusrmgui;

import ch.ge.cti.nexus.nexusrmgui.service.CertificateService;
import ch.ge.cti.nexus.nexusrmgui.service.ComponentService;

import ch.ge.cti.nexus.nexusrmgui.service.PermissionService;
import jakarta.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
@Slf4j
public class Application implements CommandLineRunner {

    int days;

    @Resource
    ApplicationContext applicationContext;

    public static void main(String[] args) throws Exception {

        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws InterruptedException {
        if (args.length > 0) {
            String option = args[0];

            switch (option) {
                case "certificates", "1":
                    var certificateService = applicationContext.getBean(CertificateService.class);
                    if (args.length > 1) {
                        days = Integer.parseInt(args[1]);
                    } else {
                        days = 30;
                    }
                    certificateService.showExpiredCertificates(days);
                    break;
                case "heavyComponents", "2":
                    var componentService = applicationContext.getBean(ComponentService.class);
                    componentService.showComponents();
                    break;
                case "permissions", "3":
                    var roleService = applicationContext.getBean(PermissionService.class);
                    if (args.length > 1) {
                        var userId = args[1].toUpperCase();
                        roleService.showPermissions(userId);
                    } else {
                        System.out.println("Please provide a user ID for the 'permission' option.");
                    }
                    break;
                case "deleteComponents", "4":
                    var componentServiceForDelete = applicationContext.getBean(ComponentService.class);
                    var dryRun = true;
                    // By default, dryRun is true. It is set to false only if the second argument is "realRun".
                    if (args.length > 1 && args[1].equalsIgnoreCase("realRun")) {
                        dryRun = false;
                    }
                    componentServiceForDelete.deleteComponents(dryRun);
                    break;
                default:
                    System.out.println("Invalid option. Use 'certificate' or '1' for certificates, 'heavyComponents' or '2' for heavyComponents, 'permissions' or '3' for permissions, 'deleteComponents' or '4' for deleteComponents.");
            }
        } else {
            System.out.println("No arguments provided. Use 'certificate' or '1' for certificates, 'heavyComponents' or '2' for heavyComponents, 'permissions' or '3' for permissions, 'deleteComponents' or '4' for deleteComponents.");
        }
    }
}
