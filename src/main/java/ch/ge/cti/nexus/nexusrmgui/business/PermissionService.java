package ch.ge.cti.nexus.nexusrmgui.business;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class PermissionService {

    @Resource
    private NexusAccessService nexusAccessService;

    public void montrerPermissions() {
        List<Permission> permissions = nexusAccessService.getPermissions();
        for (Permission permission : permissions) {
            log.info(permission.toString());
        }
    }
}
