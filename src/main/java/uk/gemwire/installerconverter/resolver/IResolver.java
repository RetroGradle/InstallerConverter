package uk.gemwire.installerconverter.resolver;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import javax.annotation.Nullable;

import uk.gemwire.installerconverter.util.maven.Artifact;
import uk.gemwire.installerconverter.util.maven.ArtifactKey;
import uk.gemwire.installerconverter.util.maven.CachedArtifactInfo;

public interface IResolver {

    @Nullable
    default CachedArtifactInfo resolve(ArtifactKey key) {
        return resolve(key.host(), key.artifact());
    }

    @Nullable
    CachedArtifactInfo resolve(String host, Artifact artifact);

    default void serialize(Writer writer) throws IOException {

    }

    default void deserialize(Reader reader) throws IOException {

    }

}
