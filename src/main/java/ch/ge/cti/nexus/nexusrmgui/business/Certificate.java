package ch.ge.cti.nexus.nexusrmgui.business;

// import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Gestion du format JSON des certificats TLS des depots.
 */
// @JsonIgnoreProperties(ignoreUnknown = true)
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

    // Getters and Setters

    public long getExpiresOn() {
        return expiresOn;
    }

    public void setExpiresOn(long expiresOn) {
        this.expiresOn = expiresOn;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getIssuedOn() {
        return issuedOn;
    }

    public void setIssuedOn(long issuedOn) {
        this.issuedOn = issuedOn;
    }

    public String getIssuerCommonName() {
        return issuerCommonName;
    }

    public void setIssuerCommonName(String issuerCommonName) {
        this.issuerCommonName = issuerCommonName;
    }

    public String getIssuerOrganization() {
        return issuerOrganization;
    }

    public void setIssuerOrganization(String issuerOrganization) {
        this.issuerOrganization = issuerOrganization;
    }

    public String getIssuerOrganizationalUnit() {
        return issuerOrganizationalUnit;
    }

    public void setIssuerOrganizationalUnit(String issuerOrganizationalUnit) {
        this.issuerOrganizationalUnit = issuerOrganizationalUnit;
    }

    public String getPem() {
        return pem;
    }

    public void setPem(String pem) {
        this.pem = pem;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getSubjectCommonName() {
        return subjectCommonName;
    }

    public void setSubjectCommonName(String subjectCommonName) {
        this.subjectCommonName = subjectCommonName;
    }

    public String getSubjectOrganization() {
        return subjectOrganization;
    }

    public void setSubjectOrganization(String subjectOrganization) {
        this.subjectOrganization = subjectOrganization;
    }

    public String getSubjectOrganizationalUnit() {
        return subjectOrganizationalUnit;
    }

    public void setSubjectOrganizationalUnit(String subjectOrganizationalUnit) {
        this.subjectOrganizationalUnit = subjectOrganizationalUnit;
    }

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