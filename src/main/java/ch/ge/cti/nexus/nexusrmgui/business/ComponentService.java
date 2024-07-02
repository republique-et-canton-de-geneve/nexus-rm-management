package ch.ge.cti.nexus.nexusrmgui.business;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
public class ComponentService {

    @Resource
    private NexusAccessService nexusAccessService;

    public void montrerComponents() {
        List<Component> components = nexusAccessService.getComponents();
        for (Component component : components) {
            log.info(component.toString());
        }
    }
}
