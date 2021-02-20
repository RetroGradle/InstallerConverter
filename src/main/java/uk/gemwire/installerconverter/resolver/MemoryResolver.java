package uk.gemwire.installerconverter.resolver;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gemwire.installerconverter.util.Hashing;
import uk.gemwire.installerconverter.util.maven.Artifact;
import uk.gemwire.installerconverter.util.maven.CachedArtifactInfo;

public class MemoryResolver extends AbstractResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(MemoryResolver.class);

    private final Path localRoot;

    public MemoryResolver(Path localRoot, @Nullable IResolver fallback) {
        super(fallback, LOGGER);

        this.localRoot = localRoot;
    }

    @Override
    @Nullable
    protected CachedArtifactInfo internalResolve(String host, Artifact artifact) throws IOException {
        String path = artifact.asPath();
        Path local = localRoot.resolve(path);

        // If we don't have a local copy return
        if (!Files.exists(local)) return null;

        LOGGER.trace("Checking for local copy of artifact {} (of host {})", artifact, host);

        try (InputStream stream = Files.newInputStream(local, StandardOpenOption.READ)) {
            // Otherwise calculate from Local
            CachedArtifactInfo fromLocal = Hashing.calculateSHA1andSize(stream, "");

            LOGGER.warn("Using local copy of artifact {} from host {}", artifact, host);
            return fromLocal;
        }
    }

    @Override
    public void serialize(Writer writer) throws IOException {
        if (fallback != null) fallback.serialize(writer);
    }

    @Override
    public void deserialize(Reader reader) throws IOException {
        if (fallback != null) fallback.deserialize(reader);
    }
}
