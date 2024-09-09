/*
 * Copyright (C) <Date> Republique et canton de Geneve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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