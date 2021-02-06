package uk.gemwire.installerconverter.v1_5;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import uk.gemwire.installerconverter.util.IConvertable;

public final class VersionInfo implements IConvertable<ObjectNode> {

    private final Map<String, JsonNode> data = new HashMap<>();

    private String id;
    private List<LibraryInfo> libraries;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<LibraryInfo> getLibraries() {
        return libraries;
    }

    public void setLibraries(List<LibraryInfo> libraries) {
        this.libraries = libraries;
    }

    public Map<String, JsonNode> getAdditionalData() {
        return data;
    }

    @JsonAnySetter
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
    public ObjectNode convert(JsonNodeFactory factory) {
        ObjectNode node = factory.objectNode();

        // Add `_comment_` node
        node.set("_comment_", Conversions.createCommentNode(factory));

        // Convert `id`
        node.put("id", Conversions.convertId(id));

        // Add Additional Data
        data.forEach(node::set);

        // Convert Libraries
        List<ObjectNode> libs = libraries.stream().map(l -> l.convert(factory)).collect(Collectors.toList());
        node.set("libraries", factory.arrayNode().addAll(libs));

        // Return VersionInfo
        return node;
    }

}