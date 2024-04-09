package ch.ge.cti.nexus.nexusrmgui;

import ch.ge.cti.nexus.nexusrmgui.business.CertificateService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication(scanBasePackages = { "ch.ge.cti.nexus.nexusrmgui" })
@Slf4j
public class Application implements CommandLineRunner {

    @Resource
    ApplicationContext applicationContext;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws InterruptedException {
        var certificateService = applicationContext.getBean(CertificateService.class);
        var val = certificateService.givePipo();
        log.info("Valeur obtenue : {}", val);
    }

}
