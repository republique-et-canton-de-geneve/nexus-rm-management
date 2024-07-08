package ch.ge.cti.nexus.nexusrmgui.business;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
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

    @Override
    public String toString() {
        return "Certificate{" +
                "expiresOn=" + expiresOn +
                ", fingerprint='" + fingerprint + '\'' +
                ", id='" + id + '\'' +
                ", issuedOn=" + issuedOn +
                ", issuerCommonName='" + issuerCommonName + '\'' +
                ", issuerOrganization='" + issuerOrganization + '\'' +
                ", issuerOrganizationalUnit='" + issuerOrganizationalUnit + '\'' +
                ", pem='" + pem + '\'' +
                ", serialNumber='" + serialNumber + '\'' +
                ", subjectCommonName='" + subjectCommonName + '\'' +
                ", subjectOrganization='" + subjectOrganization + '\'' +
                ", subjectOrganizationalUnit='" + subjectOrganizationalUnit + '\'' +
                '}';
    }
}