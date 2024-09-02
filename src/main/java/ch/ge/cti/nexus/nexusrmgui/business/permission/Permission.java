package ch.ge.cti.nexus.nexusrmgui.business.permission;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Permission {

    private String userId;

    private List<String> roles;

    private List<String> externalRoles;

}
