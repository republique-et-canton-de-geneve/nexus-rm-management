package ch.ge.cti.nexus.nexusrmgui.business;

import ch.ge.cti.nexus.nexusrmgui.utils.TestExecutionLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.BDDAssertions.then;

@ExtendWith({MockitoExtension.class, TestExecutionLogger.class})
class CertificateServiceTest {

    CertificateService certificateService;

    @BeforeEach
    void init() {
        certificateService = new CertificateService();
        certificateService.pipo = "pipooo";
    }

    @Test()
    void devrait_reussir_a_faire_dire_au_service_ce_qu_il_a_dans_le_ventre() {
        // given

        // when
        var value = certificateService.givePipo();

        // then
        then(value)
                .as("du pipo")
                .isEqualTo("pipooo");
    }

}
