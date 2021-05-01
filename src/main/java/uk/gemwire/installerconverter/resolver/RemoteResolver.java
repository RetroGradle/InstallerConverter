package uk.gemwire.installerconverter.resolver;

import java.io.IOException;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gemwire.installerconverter.util.maven.Artifact;
import uk.gemwire.installerconverter.util.maven.CachedArtifactInfo;
import uk.gemwire.installerconverter.util.maven.Maven;

public class RemoteResolver extends AbstractResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger("RemoteResolver");

    public RemoteResolver(@Nullable IResolver fallback) {
        super(fallback, LOGGER);
    }

    @Override
    @Nullable
    protected CachedArtifactInfo internalResolve(String host, Artifact artifact) throws IOException {
        LOGGER.trace("Resolving remote artifact {} from host {}", artifact, host);
        return Maven.calculateSHA1andSize(host + artifact.asPath());
    }

}
