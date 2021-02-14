package uk.gemwire.installerconverter.v1_5;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.JsonNode;
import uk.gemwire.installerconverter.util.JacksonUsed;

public final class OptionalLibrary {

    private final Map<String, JsonNode> data = new HashMap<>();

    private String name;
    private String artifact;
    //private String maven;
    //private boolean client;
    //private boolean server;
    //private boolean _default;
    //private boolean inject;
    //private String desc;
    //private String url;

    public String getDescription() {
        return name != null ? name : artifact;
    }

    @JacksonUsed
    public void setName(String name) {
        this.name = name;
    }

    @JacksonUsed
    public void setArtifact(String artifact) {
        this.artifact = artifact;
    }

    @JsonAnySetter
    @JacksonUsed
    public void setAdditionalData(String key, JsonNode value) {
        data.put(key, value);
    }
}
