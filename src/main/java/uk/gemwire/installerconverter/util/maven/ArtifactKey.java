package uk.gemwire.installerconverter.util.maven;

import java.io.IOException;
import java.util.Objects;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.MoreObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gemwire.installerconverter.Config;
import uk.gemwire.installerconverter.v1_5.LibraryInfo;
import uk.gemwire.installerconverter.v1_5.conversion.IConvertable;

public class ArtifactKey implements IConvertable<ObjectNode, Config> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactKey.class);

    private final String host;
    private final Artifact artifact;

    public ArtifactKey(String host, Artifact artifact) {
        this.host = host;
        this.artifact = artifact;
    }

    public String host() {
        return host;
    }

    public Artifact artifact() {
        return artifact;
    }

    //==================================================================================================================

    public static ArtifactKey of(String artifact) {
        return of(Maven.MOJANG, Artifact.of(artifact));
    }

    public static ArtifactKey of(String host, String artifact) {
        return of(host, Artifact.of(artifact));
    }

    public static ArtifactKey of(String host, Artifact artifact) {
        host  = host.replaceFirst("^http:", "https:");
        host += host.endsWith("/") ? "" : "/";

        return new ArtifactKey(host, artifact);
    }

    public static ArtifactKey of(LibraryInfo info) {
        return of(info.getUrl(), info.getGav());
    }

    public ArtifactKey with(String host) {
        return of(host, artifact);
    }

    public ArtifactKey with(Artifact artifact) {
        return of(host, artifact);
    }

    //==================================================================================================================

    @Override
    public void validate() throws IllegalStateException {

    }

    @Override
    public ObjectNode convert(Config config, JsonNodeFactory factory) throws IOException {
        String path = artifact.asPath();
        String finalURL = host + path;

        LOGGER.debug("Resolving: " + finalURL);

        ObjectNode node = factory.objectNode();
        node.put("path", path);

        CachedArtifactInfo data = config.resolver().resolve(this);

        if (data == null)
            throw new IOException(String.format("Couldn't get Sha1 or Size for '%s' from '%s'", artifact.asStringWithClassifier(), host));

        node.put("url", data.url());
        node.put("sha1", data.sha1Hash());
        node.put("size", data.expectedSize());

        return node;
    }

    //==================================================================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArtifactKey that = (ArtifactKey) o;
        return Objects.equals(host, that.host) && Objects.equals(artifact, that.artifact);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, artifact);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("host", host)
            .add("artifact", artifact)
            .toString();
    }
}
