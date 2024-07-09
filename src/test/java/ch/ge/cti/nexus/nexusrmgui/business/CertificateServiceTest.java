package ch.ge.cti.nexus.nexusrmgui.business;

import ch.ge.cti.nexus.nexusrmgui.utils.TestExecutionLogger;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.BDDMockito.given;


import static org.assertj.core.api.BDDAssertions.*;

@ExtendWith({MockitoExtension.class, TestExecutionLogger.class})
@Slf4j
class CertificateServiceTest {

    @Mock
    private NexusAccessService nexusAccessService;

    @InjectMocks
    private CertificateService certificateService;

    private List<Certificate> expiredCertificates;

    @BeforeEach
    void setUp() {
        long currentTime = System.currentTimeMillis();
        expiredCertificates = List.of(
                Certificate.builder()
                        .expiresOn(currentTime - 1000L)
                        .issuedOn(currentTime - 2000L)
                        .id("cert1")
                        .fingerprint("fp1")
                        .issuerCommonName("Issuer CN1")
                        .issuerOrganization("Issuer Org1")
                        .issuerOrganizationalUnit("Issuer OU1")
                        .pem("pem1")
                        .serialNumber("serial1")
                        .subjectCommonName("Subject CN1")
                        .subjectOrganization("Subject Org1")
                        .subjectOrganizationalUnit("Subject OU1")
                        .build(),
                Certificate.builder()
                        .expiresOn(currentTime + 1000L)
                        .issuedOn(currentTime - 2000L)
                        .id("cert2")
                        .fingerprint("fp2")
                        .issuerCommonName("Issuer CN2")
                        .issuerOrganization("Issuer Org2")
                        .issuerOrganizationalUnit("Issuer OU2")
                        .pem("pem2")
                        .serialNumber("serial2")
                        .subjectCommonName("Subject CN2")
                        .subjectOrganization("Subject Org2")
                        .subjectOrganizationalUnit("Subject OU2")
                        .build()
        );
    }


    @Test
    void testShowExpiredCertificates() {
        // Given
        given(nexusAccessService.getCertificats()).willReturn(expiredCertificates.toArray(new Certificate[0]));

        // When
        certificateService.showExpiredCertificates(30);

        // Then
        then(expiredCertificates).isNotNull();
    }

    @Test
    void testGetExpiredCertificates() {
        // Given
        long currentTime = System.currentTimeMillis();
        Certificate[] mockCertificates = {
                Certificate.builder()
                        .expiresOn(currentTime - 1000L)
                        .issuedOn(currentTime - 2000L)
                        .id("cert1")
                        .fingerprint("fp1")
                        .issuerCommonName("Issuer CN1")
                        .issuerOrganization("Issuer Org1")
                        .issuerOrganizationalUnit("Issuer OU1")
                        .pem("pem1")
                        .serialNumber("serial1")
                        .subjectCommonName("Subject CN1")
                        .subjectOrganization("Subject Org1")
                        .subjectOrganizationalUnit("Subject OU1")
                        .build(),
                Certificate.builder()
                        .expiresOn(currentTime + 1000L)
                        .issuedOn(currentTime - 2000L)
                        .id("cert2")
                        .fingerprint("fp2")
                        .issuerCommonName("Issuer CN2")
                        .issuerOrganization("Issuer Org2")
                        .issuerOrganizationalUnit("Issuer OU2")
                        .pem("pem2")
                        .serialNumber("serial2")
                        .subjectCommonName("Subject CN2")
                        .subjectOrganization("Subject Org2")
                        .subjectOrganizationalUnit("Subject OU2")
                        .build(),
                Certificate.builder()
                        .expiresOn(currentTime + 8 * 24 * 60 * 60 * 1000L)
                        .issuedOn(currentTime - 2000L)
                        .id("cert3")
                        .fingerprint("fp3")
                        .issuerCommonName("Issuer CN3")
                        .issuerOrganization("Issuer Org3")
                        .issuerOrganizationalUnit("Issuer OU3")
                        .pem("pem3")
                        .serialNumber("serial3")
                        .subjectCommonName("Subject CN3")
                        .subjectOrganization("Subject Org3")
                        .subjectOrganizationalUnit("Subject OU3")
                        .build()
        };

        given(nexusAccessService.getCertificats()).willReturn(mockCertificates);

        // When
        List<Certificate> expiredCertificates = certificateService.getExpiredCertificates(30);

        // Then
        then(expiredCertificates).isNotNull();
        then(expiredCertificates).hasSize(3);
        then(expiredCertificates.get(0).getId()).isEqualTo("cert3");
        then(expiredCertificates.get(1).getId()).isEqualTo("cert2");
    }


}

//    @Test()
//    void devrait_reussir_a_faire_dire_au_service_ce_qu_il_a_dans_le_ventre() {
//        // given
//
//        // when
//        var value = certificateService.montreCertificatsEchus();
//
//        // then
//        then(value)
//                .as("du pipo")
//                .isEqualTo("pipooo");
//    }


