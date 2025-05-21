package ch.ge.cti.nexus.nexusrmgui.service;

import ch.ge.cti.nexus.nexusrmgui.business.NexusAccessService;
import ch.ge.cti.nexus.nexusrmgui.business.permission.Privilege;
import ch.ge.cti.nexus.nexusrmgui.business.permission.Role;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
/**
 * REFMAV-952 Mise en ordre des droits sur les dépôts Docker.
 */
public class YdpService {

    @Resource
    private NexusAccessService nexusAccessService;

    public void getDockerRoles() {
        List<Role> roles = nexusAccessService.getRoles();

        // tous les privileges dont le repository est null
        log.info("Tous les privileges non \"nx-\" dont le repository est nul :");
        nexusAccessService.getPrivileges().stream()
                .filter(p -> !p.getName().startsWith("nx-"))
                .filter(p -> p.getRepository() == null)
                .forEach(pName -> log.info("  {}", pName));

        // toutes les valeurs de "repositories" des privileges avec info si sensible ou non
        log.info("");
        log.info("Tous les repositories mentionnes par des privileges :");
        nexusAccessService.getPrivileges().stream()
                .map(Privilege::getRepository)
                .distinct()
                .map(name -> "  (" + (isSensitiveDockerRepository(name) ? "oui" : "non") + ") " + name)
                .sorted()
                .forEach(s -> log.info("  {}", s));

        // tous les privileges portant sur des repositories Docker sensible
        log.info("");
        log.info("Tous les roles et privileges sensibles :");
        roles.stream()
                .filter(r -> ! r.getPrivileges().isEmpty())
                .peek(r -> log.info("Role [{}] : source [{}]", r.getName(), r.getSource()))
                .flatMap(r -> r.getPrivileges().stream())
                .map(pName -> nexusAccessService.getPrivilege(pName).get())
                .filter(p -> p.getType().equals("repository-content-selector"))
                .filter(p -> isSensitiveDockerRepository(p.getRepository()))
                .filter(p -> isSensitiveContentSelectorExpression(getExpression(p.getContentSelector())))
                .forEach(p -> log.info("  Privilege(name={}, repository={}, cs=({}, [{}]), actions={})",
                        p.getName(), p.getRepository(), p.getContentSelector(),
                        nexusAccessService.getContentSelector(p.getContentSelector()).get().getExpression(),
                        p.getActions()));
    }

    private static boolean isSensitiveDockerRepository(String repositoryName) {
        return repositoryName == null
                || repositoryName.equals("*")
                || repositoryName.equals("*-docker")
                || repositoryName.equals("oci-snapshot")
                || repositoryName.equals("oci-release")
                || repositoryName.equals("oci-prod");
    }

    private String getExpression(String contentSelectorName) {
        return nexusAccessService.getContentSelector(contentSelectorName).get().getExpression();
    }

    private boolean isSensitiveContentSelectorExpression(String expression) {
        return !expression.contains("format == \"nuget\"") &&
                !expression.contains("format == \"maven2\"");
    }

}
