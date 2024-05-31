package ch.ge.cti.nexus.nexusrmgui.business;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


@Service
@Slf4j
public class ComponentService {

    @Resource
    private NexusAccessService nexusAccessService;

    public void montrerComponents() {
        String componentsEnVrac = nexusAccessService.getComponents();
        log.info(componentsEnVrac);
    }

}
