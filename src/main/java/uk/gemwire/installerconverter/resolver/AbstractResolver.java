package uk.gemwire.installerconverter.resolver;

import java.io.IOException;
import javax.annotation.Nullable;

import uk.gemwire.installerconverter.util.Pair;
import uk.gemwire.installerconverter.util.maven.Artifact;

public abstract class AbstractResolver implements IResolver {

    @Nullable
    private final IResolver fallback;

    protected AbstractResolver(@Nullable IResolver fallback) {
        this.fallback = fallback;
    }

    @Override
    @Nullable
    public Pair<String, Long> resolve(String host, Artifact artifact) {
        Pair<String, Long> result = null;

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
    abstract Pair<String, Long> internalResolve(String host, Artifact artifact) throws IOException;

}
