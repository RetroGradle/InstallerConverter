package uk.gemwire.installerconverter.resolver;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import javax.annotation.Nullable;

import uk.gemwire.installerconverter.util.Hashing;
import uk.gemwire.installerconverter.util.Log;
import uk.gemwire.installerconverter.util.Maven;
import uk.gemwire.installerconverter.util.maven.Artifact;
import uk.gemwire.installerconverter.util.maven.CachedArtifactInfo;

public class LocalResolver extends AbstractResolver {

    private final Path localRoot;

    public LocalResolver(Path localRoot) {
        this(localRoot, null);
    }

    public LocalResolver(Path localRoot, @Nullable IResolver fallback) {
        super(fallback);

        this.localRoot = localRoot;
    }

    @Override
    @Nullable
    protected CachedArtifactInfo internalResolve(String host, Artifact artifact) throws IOException {
        String path = artifact.asPath();
        Path local = localRoot.resolve(path);

        // If we don't have a local copy return
        if (!Files.exists(local)) return null;

        Log.trace(String.format("Have potential Local for '%s' from '%s'", artifact.asStringWithClassifier(), host));

        try (InputStream stream = Files.newInputStream(local, StandardOpenOption.READ)) {
            // Otherwise calculate from Local
            CachedArtifactInfo fromLocal = Hashing.calculateSHA1andSize(stream);

            try {
                // Download the remote sha1
                String remoteHash = Maven.downloadSha1(new URL(host + path + ".sha1"));

                // Ugly ...
                if (remoteHash.length() == 0) throw new IOException();

                // Ensure the local sha1 is equal to the remote declared sha1
                if (!Objects.equals(fromLocal.sha1Hash(), remoteHash)) {
                    Log.trace(String.format("Local Sha1 didn't match Remote for '%s' from '%s'", artifact.asStringWithClassifier(), host));
                    return null;
                }
            } catch (IOException e) {
                Log.warn(String.format("Couldn't resolve Remote sha1 for '%s' from '%s' assuming Local is valid", artifact.asStringWithClassifier(), host));
            }

            Log.trace(String.format("Using Local for '%s' from '%s'", artifact.asStringWithClassifier(), host));
            return fromLocal;
        }
    }

}
