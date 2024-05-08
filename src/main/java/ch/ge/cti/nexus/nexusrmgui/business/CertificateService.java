package ch.ge.cti.nexus.nexusrmgui.business;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Gestion des certificats TLS des depots.
 */
@Service
@Slf4j
public class CertificateService {

    @Resource
    private NexusAccessService nexusAccessService;

    public void montreCertificatsEchus() {
        String certifsEnVrac = nexusAccessService.getCertificats();
        log.info(certifsEnVrac);
    }

}
