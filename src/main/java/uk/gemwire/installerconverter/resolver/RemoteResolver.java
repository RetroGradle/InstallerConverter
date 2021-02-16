package uk.gemwire.installerconverter.resolver;

import java.io.IOException;
import java.net.URL;
import javax.annotation.Nullable;

import uk.gemwire.installerconverter.util.Log;
import uk.gemwire.installerconverter.util.Maven;
import uk.gemwire.installerconverter.util.maven.Artifact;
import uk.gemwire.installerconverter.util.maven.CachedArtifactInfo;

public class RemoteResolver extends AbstractResolver {

    public RemoteResolver() {
        this(null);
    }

    public RemoteResolver(@Nullable IResolver fallback) {
        super(fallback);
    }

    @Override
    @Nullable
    protected CachedArtifactInfo internalResolve(String host, Artifact artifact) throws IOException {
        Log.trace(String.format("Resolving Remote for '%s' from '%s'", artifact.asStringWithClassifier(), host));
        return Maven.calculateSHA1andSize(new URL(host + artifact.asPath()));
    }

}
