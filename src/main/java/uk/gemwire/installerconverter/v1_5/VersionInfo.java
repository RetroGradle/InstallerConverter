package uk.gemwire.installerconverter.v1_5;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import uk.gemwire.installerconverter.util.IConvertable;
import uk.gemwire.installerconverter.util.JacksonUsed;

/**
 * This is actually an older https://minecraft.gamepedia.com/Client.json that we're upgrading to the newer standard
 * TODO: More Testing / Better Conversion
 */
public final class VersionInfo implements IConvertable<ObjectNode> {

    private final Map<String, JsonNode> data = new HashMap<>();

    private String id;
    private List<LibraryInfo> libraries;

    @JacksonUsed
    public void setId(String id) {
        this.id = id;
    }

    @JacksonUsed
    public void setLibraries(List<LibraryInfo> libraries) {
        this.libraries = libraries;
    }

    @JsonAnySetter
    @JacksonUsed
    public void setAdditionalData(String key, JsonNode value) {
        data.put(key, value);
    }

    @Override
    public void validate() throws AssertionError {
        if (id == null) throw new AssertionError("No Id for VersionInfo");
        for (LibraryInfo library : libraries)
            library.validate();
    }

    @Override
    public ObjectNode convert(JsonNodeFactory factory) throws IOException {
        ObjectNode node = factory.objectNode();

        // Remove `jar` if == `inheritsFrom`
        potentiallyRemoveJar();

        // Add `_comment_` node
        node.set("_comment_", Conversions.createCommentNode(factory));

        // Convert `id`
        node.put("id", Conversions.convertId(id));

        set(node, "time");
        set(node, "releaseTime");
        set(node, "type");
        set(node, "mainClass");
        set(node, "inheritsFrom");
        set(node, "logging");
        set(node, "minecraftArguments"); //TODO: Convert to `arguments` format

        //TODO: minimumLauncherVersion?

        // Add Additional Data
        data.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(e -> node.set(e.getKey(), e.getValue()));

        // Convert Libraries
        List<ObjectNode> libs = new ArrayList<>();
        for (LibraryInfo library : libraries) {
            libs.add(library.convert(factory));
        }

        node.set("libraries", factory.arrayNode().addAll(libs));

        // Return VersionInfo
        return node;
    }

    private void potentiallyRemoveJar() {
        if (!data.containsKey("jar")) return;
        if (!data.containsKey("inheritsFrom")) return;
        if (!Objects.equals(data.get("jar").asText(), data.get("inheritsFrom").asText())) return;

        data.remove("jar");
    }

    private void set(ObjectNode node, String name) {
        if (data.containsKey(name)) node.set(name, data.remove(name));
    }

}