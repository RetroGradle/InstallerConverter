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
import uk.gemwire.installerconverter.util.JacksonUsed;
import uk.gemwire.installerconverter.util.manifest.VersionManifest;
import uk.gemwire.installerconverter.util.maven.Artifact;
import uk.gemwire.installerconverter.util.maven.CachedArtifactInfo;

/**
 * This is actually an older https://minecraft.gamepedia.com/Client.json that we're upgrading to the newer standard
 * TODO: More Testing / Better Conversion
 */
public final class VersionInfo implements IConvertable<ObjectNode, CommonContext> {

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
    public void validate() throws IllegalStateException {
        if (id == null) throw new IllegalStateException("No Id for VersionInfo");
        for (LibraryInfo library : libraries)
            library.validate();
    }

    @Override
    public ObjectNode convert(CommonContext context, JsonNodeFactory factory) throws IOException {
        ObjectNode node = factory.objectNode();

        // Legacy Forge Conversion (1.6.x / 1.5.x)
        if (libraries.stream().map(LibraryInfo::getGav).map(Artifact::artifact).anyMatch(artifact -> Objects.equals("minecraftforge", artifact))) {
            /* Standardised Legacy Libraries */
            libraries.forEach(library -> library.standardise(context.minecraft()));

            List<String> inheritLibraries = VersionManifest.provideLibraries(context.minecraft());
            libraries.removeIf(library -> inheritLibraries.contains(library.getGav().asStringWithClassifier()));

            if (!data.containsKey("inheritsFrom")) data.put("inheritsFrom", factory.textNode(context.minecraft()));
        }

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

        if (context.client() != null || context.server() != null) {
            ObjectNode downloads = factory.objectNode();
            if (context.client() != null) addDownload(factory, downloads, context.client(), context.minecraft(), "client");
            if (context.server() != null) addDownload(factory, downloads, context.server(), context.minecraft(), "server");
            data.put("downloads", downloads);
        }

        // Add Additional Data
        data.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(e -> node.set(e.getKey(), e.getValue()));

        // Convert Libraries
        List<ObjectNode> libs = new ArrayList<>();
        for (LibraryInfo library : libraries) {
            libs.add(library.convert(context, factory));
        }

        node.set("libraries", factory.arrayNode().addAll(libs));

        // Return VersionInfo
        return node;
    }

    private void addDownload(JsonNodeFactory factory, ObjectNode downloads, CachedArtifactInfo info, String minecraft, String type) {
        ObjectNode artifact = factory.objectNode();
        artifact.put("path", "net/minecraft/{type}/{version}/{type}-{version}-stripped.jar".replace("{type}", type).replace("{version}", minecraft));
        artifact.put("url", info.url());
        artifact.put("sha1", info.sha1Hash());
        artifact.put("size", info.expectedSize());
        downloads.set(type, artifact);
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