package ch.ge.cti.nexus.nexusrmgui.business;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Asset {
    private long fileSize;
    private String lastModified;
    private String path;
}