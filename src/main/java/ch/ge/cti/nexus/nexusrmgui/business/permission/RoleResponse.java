package ch.ge.cti.nexus.nexusrmgui.business.permission;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoleResponse {

    private String id;
    private String source;
    private String name;
    private String description;
    private boolean readOnly;
    private List<String> privileges;
    private List<String> roles;
    private List<String> externalRoles;
}
