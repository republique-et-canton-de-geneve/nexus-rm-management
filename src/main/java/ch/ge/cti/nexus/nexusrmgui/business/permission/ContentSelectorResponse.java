package ch.ge.cti.nexus.nexusrmgui.business.permission;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContentSelectorResponse {

    private String name;
    private String type;
    private String description;
    private String expression;
}
