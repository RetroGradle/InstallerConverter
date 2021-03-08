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
import com.google.common.collect.Lists;
import uk.gemwire.installerconverter.util.Jackson;
import uk.gemwire.installerconverter.util.JacksonUsed;
import uk.gemwire.installerconverter.util.manifest.VersionManifest;
import uk.gemwire.installerconverter.util.maven.Artifact;
import uk.gemwire.installerconverter.util.maven.ArtifactKey;
import uk.gemwire.installerconverter.util.maven.CachedArtifactInfo;
import uk.gemwire.installerconverter.v1_5.conversion.CommonContext;
import uk.gemwire.installerconverter.v1_5.conversion.Conversions;
import uk.gemwire.installerconverter.v1_5.conversion.IConvertable;

/**
 * This is actually an older https://minecraft.gamepedia.com/Client.json that we're upgrading to the newer standard
 * TODO: More Testing / Better Conversion
 */
public final class VersionInfo implements IConvertable<ObjectNode, CommonContext> {

    private final Map<String, JsonNode> data = new HashMap<>();

    private String id;
    private List<LibraryInfo> libraries = Lists.newArrayList();

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

    public void addLibrary(ArtifactKey key) {
        addLibrary(LibraryInfo.of(key));
    }

    public void addLibrary(LibraryInfo library) {
        libraries.add(library);
    }

    @Override
    public void validate() throws IllegalStateException {
        if (id == null) throw new IllegalStateException("No Id for VersionInfo");

        fillDefaults(Jackson.factory());

        for (LibraryInfo library : libraries)
            library.validate();
    }

    private void fillDefaults(JsonNodeFactory factory) {
        JsonNode date = factory.textNode("1960-01-01T00:00:00-0700");

        data.putIfAbsent("time", date);
        data.putIfAbsent("releaseTime", date);
        data.putIfAbsent("type", factory.textNode("release"));
        data.putIfAbsent("mainClass", factory.textNode("net.minecraft.launchwrapper.Launch"));
        data.putIfAbsent("minimumLauncherVersion", factory.numberNode(4));
    }

    @Override
    public ObjectNode convert(CommonContext context, JsonNodeFactory factory) throws IOException {
        ObjectNode node = factory.objectNode();

        // Legacy Forge Conversion (1.6.x / 1.5.x)
        if (libraries.stream().map(LibraryInfo::getGav).map(Artifact::artifact).anyMatch(artifact -> Objects.equals("minecraftforge", artifact))) {
            /* Standardised Legacy Libraries */
            libraries.forEach(library -> library.standardise(context.minecraft()));

            List<String> inheritLibraries = VersionManifest.provideLibraries(context.minecraft());

            //TODO: isServerReq is used so we don't drop ow2.asm which isn't present in the server side libraries
            libraries.removeIf(library -> !library.isServerReq() && inheritLibraries.contains(library.getGav().asStringWithClassifier()));

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
            if (context.client() != null) addDownload(factory, downloads, context.client(), "client");
            if (context.server() != null) addDownload(factory, downloads, context.server(), "server");
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

    private void addDownload(JsonNodeFactory factory, ObjectNode downloads, CachedArtifactInfo info, String type) {
        ObjectNode artifact = factory.objectNode();
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