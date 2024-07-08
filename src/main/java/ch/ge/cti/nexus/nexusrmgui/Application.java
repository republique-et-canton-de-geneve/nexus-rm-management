package ch.ge.cti.nexus.nexusrmgui;

import ch.ge.cti.nexus.nexusrmgui.business.CertificateService;
import ch.ge.cti.nexus.nexusrmgui.business.ComponentService;

import ch.ge.cti.nexus.nexusrmgui.business.PermissionService;
import jakarta.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
@Slf4j
public class Application implements CommandLineRunner {

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
                case "certificate", "1":
                    var certificateService = applicationContext.getBean(CertificateService.class);
                    certificateService.showExpiredCertificates();
                    break;
                case "component", "2":
                    var userService = applicationContext.getBean(ComponentService.class);
                    userService.montrerComponents();
                    break;
                case "permission", "3":
                    var roleService = applicationContext.getBean(PermissionService.class);
                    roleService.montrerPermissions();
                    break;
                default:
                    System.out.println("Invalid option. Use 'user' or '2' for users, 'certificate' or '1' for certificates.");
            }
        } else {
            System.out.println("No arguments provided. Use 'user' or '2' for users, 'certificate' or '1' for certificates.");
        }
    }
}
