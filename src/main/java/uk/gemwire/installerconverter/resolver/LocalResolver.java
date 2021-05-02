package uk.gemwire.installerconverter.resolver;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import javax.annotation.Nullable;

import lombok.extern.slf4j.Slf4j;
import uk.gemwire.installerconverter.util.Hashing;
import uk.gemwire.installerconverter.util.maven.Artifact;
import uk.gemwire.installerconverter.util.maven.CachedArtifactInfo;
import uk.gemwire.installerconverter.util.maven.Maven;

@Slf4j(topic = "LocalResolver")
public class LocalResolver extends AbstractResolver {
    private final Path localRoot;

    public LocalResolver(Path localRoot, @Nullable IResolver fallback) {
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
            CachedArtifactInfo fromLocal = Hashing.calculateSHA1andSize(stream, host + path);

            if (artifact.group().startsWith("uk.gemwire")) {
                log.trace("Using local copy of artifact {} from host {}", artifact, host);
                return fromLocal;
            }

            try {
                // Download the remote sha1
                String remoteHash = Maven.downloadSha1(new URL(host + path + ".sha1"));

                // Ugly ...
                if (remoteHash.length() == 0) throw new IOException();

                // Ensure the local sha1 is equal to the remote declared sha1
                if (!Objects.equals(fromLocal.sha1Hash(), remoteHash)) {
                    log.trace("Local SHA-1 hash for artifact {} did not match remote hash from host {}", artifact, host);
                    return null;
                }
            } catch (IOException e) {
                log.warn("Could not resolve remote hash for artifact {} from host {}; assuming local is valid", artifact, host, e);
            }

            log.trace("Using local copy of artifact {} from host {}", artifact, host);
            return fromLocal;
        }
    }

}
