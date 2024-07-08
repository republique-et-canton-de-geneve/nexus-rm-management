package ch.ge.cti.nexus.nexusrmgui.business;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Permission {

    private String userId;

    private List<String> roles;

    private List<String> externalRoles;




//    @Override
//    public int compareTo(Certificate o) {
//        return Long.compare(this.expiresOn, o.expiresOn);
//    }

    @Override
    public String toString() {
        return "Permission{" +
                "userId=" + userId +
                ", roles=" + roles +
                ", externalRoles=" + externalRoles +
                '}';
    }
}