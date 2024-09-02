package ch.ge.cti.nexus.nexusrmgui.business.component;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ComponentResponse {
    private List<Component> items;
    private String continuationToken;
}