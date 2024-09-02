package ch.ge.cti.nexus.nexusrmgui.business.certificate;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;


@Data
@Builder
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Certificate implements Comparable<Certificate> {
    private long expiresOn;
    private String fingerprint;
    private String id;
    private long issuedOn;
    private String issuerCommonName;
    private String issuerOrganization;
    private String issuerOrganizationalUnit;
    private String pem;
    private String serialNumber;
    private String subjectCommonName;
    private String subjectOrganization;
    private String subjectOrganizationalUnit;


    @Override
    public int compareTo(Certificate o) {
        return Long.compare(this.expiresOn, o.expiresOn);
    }

}