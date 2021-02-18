package uk.gemwire.installerconverter.v1_5;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gemwire.installerconverter.Config;
import uk.gemwire.installerconverter.util.IConvertable;
import uk.gemwire.installerconverter.util.JacksonUsed;
import uk.gemwire.installerconverter.util.maven.Artifact;
import uk.gemwire.installerconverter.util.maven.CachedArtifactInfo;

public final class LibraryInfo implements IConvertable<ObjectNode, Config> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LibraryInfo.class);

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
        this.url = url.replaceFirst("^http:", "https:") + (url.endsWith("/") ? "" : "/");
    }

    @Override
    public void validate() throws IllegalStateException {
        if (gav == null) throw new IllegalStateException("No Name for LibraryInfo");
    }

    @Override
    public ObjectNode convert(Config config, JsonNodeFactory factory) throws IOException {
        //TODO: Handle clientreq / serverreq - What do we need to do with them

        ObjectNode downloads = factory.objectNode();

        //TODO: Testing
        if (gav.classifier() != null) {
            ObjectNode classifiers = factory.objectNode();
            classifiers.set(gav.classifier(), convertArtifact(config, factory));

            downloads.set("classifiers", classifiers);
        } else {
            downloads.set("artifact", convertArtifact(config, factory));
        }

        ObjectNode node = factory.objectNode();
        node.put("name", gav.asString());
        node.set("downloads", downloads);

        return node;
    }

    public ObjectNode convertArtifact(Config config, JsonNodeFactory factory) throws IOException {
        boolean isForge = Objects.equals(gav.artifact(), "forge");

        String path = gav.asPath();
        String finalURL = url + path;

        LOGGER.debug("Resolving: " + finalURL);

        ObjectNode artifact = factory.objectNode();
        artifact.put("path", path);
        artifact.put("url", isForge ? "" : finalURL);
        //TODO: The forge gav should be grabbed from the inMemoryFs if possible
        CachedArtifactInfo data = config.resolver().resolve(isForge ? config.baseMaven() : url, isForge ? Artifact.of(gav.asString() + ":universal") : gav);

        if (data == null) throw new IOException(String.format("Couldn't get Sha1 or Size for '%s' from '%s'", gav.asStringWithClassifier(), url));

        artifact.put("sha1", data.sha1Hash());
        artifact.put("size", data.expectedSize());

        return artifact;
    }
}
