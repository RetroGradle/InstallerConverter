package uk.gemwire.installerconverter.resolver;

import java.io.IOException;
import javax.annotation.Nullable;

import uk.gemwire.installerconverter.util.maven.Artifact;
import uk.gemwire.installerconverter.util.maven.CachedArtifactInfo;

public abstract class AbstractResolver implements IResolver {

    @Nullable
    private final IResolver fallback;

    protected AbstractResolver(@Nullable IResolver fallback) {
        this.fallback = fallback;
    }

    @Override
    @Nullable
    public CachedArtifactInfo resolve(String host, Artifact artifact) {
        CachedArtifactInfo result = null;

        try { //TODO: Log / Cleanup
            result = internalResolve(host, artifact);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (result == null && fallback != null)
            result = fallback.resolve(host, artifact);

        return result;
    }

    @Nullable
    protected abstract CachedArtifactInfo internalResolve(String host, Artifact artifact) throws IOException;

}
