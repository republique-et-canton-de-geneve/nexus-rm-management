package ch.ge.cti.nexus.nexusrmgui.business;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;
import java.util.OptionalLong;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Component {
    private String id;
    private String group;
    private String name;
    private String version;
    private List<Asset> assets;

    public long getMaxAssetSize() {
        return assets.stream()
                .mapToLong(Asset::getFileSize)
                .max()
                .orElse(0L);
    }
}