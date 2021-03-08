package uk.gemwire.installerconverter.util.maven;

import java.io.IOException;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gemwire.installerconverter.Config;
import uk.gemwire.installerconverter.v1_5.LibraryInfo;
import uk.gemwire.installerconverter.v1_5.conversion.IConvertable;

public record ArtifactKey(String host, Artifact artifact) implements IConvertable<ObjectNode, Config> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactKey.class);

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

        CachedArtifactInfo data = config.resolver().resolve(host, artifact);

        if (data == null)
            throw new IOException(String.format("Couldn't get Sha1 or Size for '%s' from '%s'", artifact.asStringWithClassifier(), host));

        node.put("url", data.url());
        node.put("sha1", data.sha1Hash());
        node.put("size", data.expectedSize());

        return node;
    }

}
