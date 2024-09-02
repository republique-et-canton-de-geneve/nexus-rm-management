package ch.ge.cti.nexus.nexusrmgui.business.component;

import ch.ge.cti.nexus.nexusrmgui.util.NexusServicesLocalDateTimeDeserializer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Asset {

    private long fileSize;

    //@JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonDeserialize(using = NexusServicesLocalDateTimeDeserializer.class)
    private LocalDateTime lastModified;

    private String path;
}