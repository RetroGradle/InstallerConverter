package uk.gemwire.installerconverter.resolver;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import javax.annotation.Nullable;

import lombok.extern.slf4j.Slf4j;
import uk.gemwire.installerconverter.util.Hashing;
import uk.gemwire.installerconverter.util.maven.Artifact;
import uk.gemwire.installerconverter.util.maven.CachedArtifactInfo;

@Slf4j(topic = "MemoryResolver")
public class MemoryResolver extends AbstractResolver {

    private final Path localRoot;

    public MemoryResolver(Path localRoot, @Nullable IResolver fallback) {
        super(fallback, log);

        this.localRoot = localRoot;
    }

    @Override
    @Nullable
    protected CachedArtifactInfo internalResolve(String host, Artifact artifact) throws IOException {
        String path = artifact.asPath();
        Path local = localRoot.resolve(path);

        // If we don't have a local copy return
        if (!Files.exists(local)) return null;

        log.trace("Checking for local copy of artifact {} (of host {})", artifact, host);

        try (InputStream stream = Files.newInputStream(local, StandardOpenOption.READ)) {
            // Otherwise calculate from Local
            CachedArtifactInfo fromLocal = Hashing.calculateSHA1andSize(stream, "");

            log.trace("Using local copy of artifact {} from host {}", artifact, host);
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
