package uk.gemwire.installerconverter.resolver;

import java.io.IOException;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import uk.gemwire.installerconverter.util.maven.Artifact;
import uk.gemwire.installerconverter.util.maven.CachedArtifactInfo;

public abstract class AbstractResolver implements IResolver {

    @Nullable
    private final IResolver fallback;
    private final Logger logger;

    protected AbstractResolver(@Nullable IResolver fallback, Logger logger) {
        this.fallback = fallback;
        this.logger = logger;
    }

    @Override
    @Nullable
    public CachedArtifactInfo resolve(String host, Artifact artifact) {
        CachedArtifactInfo result = null;

        try {
            result = internalResolve(host, artifact);
        } catch (IOException e) {
            logger.error("Resolve failed with exception", e);
        }

        if (result == null && fallback != null)
            result = fallback.resolve(host, artifact);

        return result;
    }

    @Nullable
    protected abstract CachedArtifactInfo internalResolve(String host, Artifact artifact) throws IOException;

}
