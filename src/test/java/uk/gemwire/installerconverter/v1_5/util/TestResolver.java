package uk.gemwire.installerconverter.v1_5.util;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gemwire.installerconverter.resolver.AbstractResolver;
import uk.gemwire.installerconverter.resolver.IResolver;
import uk.gemwire.installerconverter.util.maven.Artifact;
import uk.gemwire.installerconverter.util.maven.ArtifactKey;
import uk.gemwire.installerconverter.util.maven.CachedArtifactInfo;

public class TestResolver extends AbstractResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestResolver.class);

    private final Map<ArtifactKey, CachedArtifactInfo> provider = new HashMap<>();

    public TestResolver(@Nullable IResolver fallback) {
        super(fallback, LOGGER);
    }

    public TestResolver add(String host, Artifact artifact, CachedArtifactInfo result) {
        provider.put(ArtifactKey.of(host, artifact), result);
        return this;
    }

    @Nullable
    @Override
    protected CachedArtifactInfo internalResolve(String host, Artifact artifact) {
        return provider.get(ArtifactKey.of(host, artifact));
    }

}
