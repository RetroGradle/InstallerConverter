package uk.gemwire.installerconverter.v1_5;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import uk.gemwire.installerconverter.util.IConvertable;
import uk.gemwire.installerconverter.util.JacksonUsed;
import uk.gemwire.installerconverter.util.Maven;
import uk.gemwire.installerconverter.util.Pair;
import uk.gemwire.installerconverter.util.maven.Artifact;

public final class LibraryInfo implements IConvertable<ObjectNode> {
    private Artifact gav;
    private List<String> checksums;
    private boolean clientreq = false;
    private boolean serverreq = false;
    private String url = "https://libraries.minecraft.net/";

    @JacksonUsed
    public void setName(String name) {
        this.gav = Artifact.of(name);
    }

    @JacksonUsed
    public void setChecksums(List<String> checksums) {
        this.checksums = checksums;
    }

    @JacksonUsed
    public void setClientreq(boolean clientreq) {
        this.clientreq = clientreq;
    }

    @JacksonUsed
    public void setServerreq(boolean serverreq) {
        this.serverreq = serverreq;
    }

    @JacksonUsed
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public void validate() throws IllegalStateException {
        if (gav == null) throw new AssertionError("No Name for LibraryInfo");
    }

    @Override
    public ObjectNode convert(JsonNodeFactory factory) throws IOException {
        //TODO: Handle clientreq / serverreq - What do we need to do with them

        ObjectNode downloads = factory.objectNode();

        //TODO: Testing
        if (gav.classifier() != null) {
            ObjectNode classifiers = factory.objectNode();
            classifiers.set(gav.classifier(), convertArtifact(factory));

            downloads.set("classifiers", classifiers);
        } else {
            downloads.set("artifact", convertArtifact(factory));
        }

        ObjectNode node = factory.objectNode();
        node.put("name", gav.asString());
        node.set("downloads", downloads);

        return node;
    }

    public ObjectNode convertArtifact(JsonNodeFactory factory) throws IOException {
        //TODO: We need to special case the :forge: dependency
        //TODO: Forge Dependency
        // URL = ""
        // Calculated Sha1/Size is for `-universal`

        String path = gav.asPath();
        String finalURL = url.replaceFirst("^http:", "https:") + path;

        ObjectNode artifact = factory.objectNode();
        artifact.put("path", path);
        artifact.put("url", finalURL);

        System.out.println("Downloading: " + finalURL);

        // TODO: Caching - We need to cache on `BASEURL:ARTIFACT` (over several runs).
        //  We should try local maven first
        //  Probably should check the sha1 of that against the `.sha1` on remote maven before using
        //  (This is mainly due to the fact that we know that Mojang host `alternatives` for certain libraries)
        Pair<String, Long> data = Objects.equals(gav.artifact(), "forge") ? Pair.of("{SHA1}", 0L) :  Maven.calculateSHA1andSize(new URL(finalURL));

        artifact.put("sha1", data.left()); // checksums != null ? checksums.get(0) : "{SHA1}");
        artifact.put("size", data.right());

        return artifact;
    }
}