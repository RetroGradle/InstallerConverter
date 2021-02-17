package uk.gemwire.installerconverter.v1_5;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.JsonNode;
import uk.gemwire.installerconverter.util.JacksonUsed;

public final class OptionalLibrary {

    private static final Collector<CharSequence, ?, String> COLLECTOR = Collectors.joining(", ", ", ", "");

    // Known Values:
    // - maven   : String
    // - client  : boolean
    // - server  : boolean
    // - default : boolean
    // - inject  : boolean
    // - desc    : String
    // - url     : String
    private final Map<String, JsonNode> data = new HashMap<>();

    private String name;
    private String artifact;

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

    @Override
    public String toString() {
        return "OptionalLibrary{" +
            "name='" + name + '\'' +
            ", artifact='" + artifact + '\'' +
            data.entrySet().stream().map((e) -> e.getKey() + "='" + e.getValue() + '\'').collect(COLLECTOR) +
            '}';
    }
}
