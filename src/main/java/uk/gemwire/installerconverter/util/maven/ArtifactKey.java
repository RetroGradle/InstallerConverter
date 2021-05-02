package uk.gemwire.installerconverter.util.maven;

import java.io.IOException;
import javax.annotation.Nonnull;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Value;
import lombok.With;
import lombok.extern.slf4j.Slf4j;
import uk.gemwire.installerconverter.Config;
import uk.gemwire.installerconverter.v1_5.LibraryInfo;
import uk.gemwire.installerconverter.v1_5.conversion.IConvertable;

@With
@Value
@Slf4j(topic = "ArtifactKey")
public class ArtifactKey implements IConvertable<ObjectNode, Config> {

    @Nonnull String host;
    @Nonnull Artifact artifact;

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

    //==================================================================================================================

    @Override
    public void validate() throws IllegalStateException {

    }

    @Override
    public ObjectNode convert(Config config, JsonNodeFactory factory) throws IOException {
        String path = artifact.asPath();
        String finalURL = host + path;

        log.debug("Resolving: " + finalURL);

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
}
