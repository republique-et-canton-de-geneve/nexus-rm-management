package ch.ge.cti.nexus.nexusrmgui.business;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Gestion des certificats TLS des depots.
 */
@Service
@Slf4j
public class CertificateService {

    @Value("${app.pipo}")
    String pipo;

    // temp
    public String givePipo() {
        return pipo;
    }

}
