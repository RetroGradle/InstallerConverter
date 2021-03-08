package uk.gemwire.installerconverter.resolver;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gemwire.installerconverter.resolver.retrotools.StrippedMinecraft;
import uk.gemwire.installerconverter.util.maven.Artifact;
import uk.gemwire.installerconverter.util.maven.CachedArtifactInfo;
import uk.gemwire.installerconverter.util.maven.Maven;

public class LegacyFMLResolver extends AbstractResolver {

    private static final Map<Artifact, String> KNOWN_LIBRARIES = new HashMap<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(LegacyFMLResolver.class);

    static {
        KNOWN_LIBRARIES.put(Artifact.of("net.sourceforge.argo:argo:3.2-small"), "https://files.minecraftforge.net/fmllibs/argo-small-3.2.jar");
    }

    public LegacyFMLResolver(@Nullable IResolver fallback) {
        super(fallback, LOGGER);
    }

    @Override
    @Nullable
    protected CachedArtifactInfo internalResolve(String host, Artifact artifact) throws IOException {
        if (Objects.equals(Maven.FAKE, host)) {
            if (Objects.equals(artifact.classifier(), "stripped")) return StrippedMinecraft.provide(artifact);

            return null;
        }

        if (!KNOWN_LIBRARIES.containsKey(artifact)) return null;

        LOGGER.trace("Resolving remote artifact {} from host {}", artifact, host);
        return Maven.calculateSHA1andSize(KNOWN_LIBRARIES.get(artifact));
    }

}
