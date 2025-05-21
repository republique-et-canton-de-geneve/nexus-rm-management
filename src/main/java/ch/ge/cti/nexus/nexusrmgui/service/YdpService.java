package ch.ge.cti.nexus.nexusrmgui.service;

import ch.ge.cti.nexus.nexusrmgui.business.NexusAccessService;
import ch.ge.cti.nexus.nexusrmgui.business.permission.Privilege;
import ch.ge.cti.nexus.nexusrmgui.business.permission.Role;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Locale.ENGLISH;

@Service
@Slf4j
/**
 * REFMAV-952 Mise en ordre des droits sur les dépôts Docker.
 */
public class YdpService {

    @Resource
    private PermissionService permissionService;

    @Resource
    private NexusAccessService nexusAccessService;

    @Value("${app.nexus.repo.d}")
    private String repoD;

    @Value("${app.nexus.repo.s}")
    private String repoS;

    @Value("${app.nexus.repo.r}")
    private String repoR;

    @Value("${app.nexus.repo.p}")
    private String repoP;

    @Value("${app.nexus.content-selector.prefix}")
    private String contentSelectorPrefix;

    @Value("${app.nexus.role.prefix}")
    private String rolePrefix;

    public void getSensitiveDockerPrivileges() {
        List<Role> roles = nexusAccessService.getRoles();

        // preparation
        var usersByRole = permissionService.getUsersByRole();

        // tous les privileges dont le repository est null
        log.info("Tous les privileges non \"nx-\" dont le repository est nul :");
        nexusAccessService.getPrivileges().stream()
                .filter(p -> !p.getName().startsWith("nx-"))
                .filter(p -> p.getRepository() == null)
                .forEach(pName -> log.info("  {}", pName));

        // toutes les valeurs de "repositories" des privileges avec info si sensible ou non
        log.info("");
        log.info("Tous les repositories mentionnes par des privileges (avec sensibilité oui/non) :");
        nexusAccessService.getPrivileges().stream()
                .map(Privilege::getRepository)
                .distinct()
                .map(name -> "  (" + (isSensitiveDockerRepository(name) ? "oui" : "non") + ") " + name)
                .sorted()
                .forEach(s -> log.info("  {}", s));

        // tous les privileges portant sur des repositories Docker sensibles
        log.info("");
        log.info("Tous les roles et privileges sensibles :");
        roles.stream()
                .filter(r -> ! r.getPrivileges().isEmpty())
                .peek(r ->
                    log.info("Role(name={}, source={}, users={})",
                            r.getName(), r.getSource(), getShortListOfUsers(usersByRole, r)))
                .flatMap(r -> r.getPrivileges().stream())
                .map(pName -> nexusAccessService.getPrivilege(pName).get())
                .filter(p -> p.getType().equals("repository-content-selector"))
                .filter(p -> isSensitiveDockerRepository(p.getRepository()))
                .filter(p -> isSensitiveContentSelectorExpression(getExpression(p.getContentSelector())))
                .forEach(p ->
                        log.info("    Privilege(name={}, repository={}, cs=({}, [{}]), actions={})",
                            p.getName(), p.getRepository(), p.getContentSelector(),
                            nexusAccessService.getContentSelector(p.getContentSelector()).get().getExpression(),
                            p.getActions()
                    ));

        // tous les privileges portant plus specifiquement sur le repository Docker "repoP"
        log.info("");
        log.info("Tous les roles et privileges sur " + repoP + " :");
        roles.stream()
                .filter(r -> ! r.getPrivileges().isEmpty())
                .peek(r ->
                        log.info("Role(name={}, source={}, users={})",
                                r.getName(), r.getSource(), getShortListOfUsers(usersByRole, r)))
                .flatMap(r -> r.getPrivileges().stream())
                .map(pName -> nexusAccessService.getPrivilege(pName).get())
                .filter(p -> p.getType().equals("repository-content-selector"))
                .filter(p -> isSensitiveDockerRepository(p.getRepository()))
                .filter(p -> !p.getRepository().equals(repoD))
                .filter(p -> !p.getRepository().equals(repoS))
                .filter(p -> !p.getRepository().equals(repoR))
                .filter(p -> isSensitiveContentSelectorExpression(getExpression(p.getContentSelector())))
                .forEach(p -> log.info("    Privilege(name={}, repository={}, cs=({}, [{}]), actions={})",
                        p.getName(), p.getRepository(), p.getContentSelector(),
                        nexusAccessService.getContentSelector(p.getContentSelector()).get().getExpression(),
                        p.getActions()));
    }

    private String getShortListOfUsers(Map<String, Set<String>> usersByRole, Role r) {
        if (usersByRole.get(r.getName()) == null) {
            return "";
        } else {
            final int MAX_USERS = 6;
            String ret = usersByRole.get(r.getName()).stream()
                    .limit(MAX_USERS)
                    .collect(Collectors.joining(","));
            if (StringUtils.countMatches(ret, ",") == MAX_USERS - 1) {
                ret = ret + "...";
            }
            return ret;
        }
    }

    public void fixDockerPrivileges() {
        nexusAccessService.getRoles().stream()
                .filter(r -> r.getName().startsWith(rolePrefix))
                .peek(r-> log.info("Role {}", r.getName()))
                .forEach(this::fixDockerPrivileges);
    }

    private void fixDockerPrivileges(Role role) {
        // correction des privileges
        if (role.getName().startsWith(rolePrefix)) {
            if (! role.getPrivileges().isEmpty()) {
                var sector = role.getName().substring(rolePrefix.length());
                addDockerPrivilege(sector, repoD);
                addDockerPrivilege(sector, repoS);
                addDockerPrivilege(sector, repoR);
                removeCatchAllDockerPrivilege(sector);
            } else {
                log.info("Role {} sans privileges : on ne fait rien", role.getName());
            }
        } else {
            log.info("Role {} pas de type {}XXX", role.getName(), rolePrefix);
        }
    }

    private void addDockerPrivilege(String sector, String repository) {
        // 1. Condition : le content selector doit exister
        var repositoryInPrivilegeName = repository.toUpperCase(ENGLISH).replaceAll("-", "_");
        var privilegeName = sector.toUpperCase(ENGLISH) + "_" + repositoryInPrivilegeName;
        var csName = contentSelectorPrefix + sector.toUpperCase(ENGLISH);
        if (nexusAccessService.getContentSelector(csName, false).isEmpty()) {
            log.info("Pas de content selector {} : on ne cree pas le privilege {}", csName, privilegeName);
            return;
        }

        // 2. Creation du privilege
        Privilege privilege = new Privilege();
        privilege.setName(privilegeName);
        privilege.setDescription(sector + " Docker sur " + repository);
        privilege.setActions(Arrays.asList("BROWSE", "READ", "EDIT", "ADD", "DELETE"));
        privilege.setFormat("docker");
        privilege.setRepository(repository);
        privilege.setContentSelector(csName);

        if (nexusAccessService.getPrivilege(privilegeName).isPresent()) {
            log.info("Le privilege [{}] existe deja", privilegeName);
            return;
        } else {
            nexusAccessService.createPrivilege(privilege);
            log.info("Privilege [{}] cree", privilegeName);
        }

        // 3. Ajout du nouveau privilege au role
        var roleName = rolePrefix + sector;
        var role = nexusAccessService.getRole(roleName).get();
        role.getPrivileges().add(privilegeName);
        nexusAccessService.updateRole(role);
    }

    private void removeCatchAllDockerPrivilege(String sector) {
        var name = "CTI_" + sector.toUpperCase() + "_Docker";
        if (nexusAccessService.getPrivilege(name).isPresent()) {
            nexusAccessService.removePrivilege(name);
            log.info("Privilege [{}] detruit", name);
        } else {
            log.info("Le privilege [{}] n'existe pas, on ne peut donc pas le detruire", name);
        }
    }

    private boolean isSensitiveDockerRepository(String repositoryName) {
        return repositoryName == null
                || repositoryName.equals("*")
                || repositoryName.equals("*-docker")
                || repositoryName.equals(repoD)
                || repositoryName.equals(repoS)
                || repositoryName.equals(repoR)
                || repositoryName.equals(repoP);
    }

    private String getExpression(String contentSelectorName) {
        return nexusAccessService.getContentSelector(contentSelectorName).get().getExpression();
    }

    private boolean isSensitiveContentSelectorExpression(String expression) {
        return !expression.contains("format == \"nuget\"") &&
                !expression.contains("format == \"maven2\"") &&
                !expression.contains("format == \"raw\"");
    }

}
