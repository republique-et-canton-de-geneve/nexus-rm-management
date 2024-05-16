package ch.ge.cti.nexus.nexusrmgui.business;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/*******************************************************************************************************************************
 *           !!! Temporaire !!! Classe a supprimer (utiliser pour le moment afin d'essayer le switch dans Application.java).
 ******************************************************************************************************************************/
@Service
@Slf4j
public class UserService {

    @Resource
    private NexusAccessService nexusAccessService;

    public void montrerUsers() {
        String usersEnVrac = nexusAccessService.getUsers();
        log.info(usersEnVrac);
    }

}
