package uk.gemwire.installerconverter.resolver;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gemwire.installerconverter.legacy.LibraryTransformers;
import uk.gemwire.installerconverter.util.maven.Artifact;
import uk.gemwire.installerconverter.util.maven.CachedArtifactInfo;
import uk.gemwire.installerconverter.util.maven.Maven;

public class LegacyFMLResolver extends AbstractResolver {

    private static final Map<Artifact, String> KNOWN_LIBRARIES = new HashMap<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(LegacyFMLResolver.class);

    static {
        KNOWN_LIBRARIES.put(Artifact.of("org.scala-lang:scala-library:2.10.0-custom"), "https://files.minecraftforge.net/fmllibs/scala-library.jar.stash");
        KNOWN_LIBRARIES.put(Artifact.of("net.sourceforge.argo:argo:3.2-small"), "https://files.minecraftforge.net/fmllibs/argo-small-3.2.jar");
    }

    public LegacyFMLResolver(@Nullable IResolver fallback) {
        super(fallback, LOGGER);
    }

    @Override
    @Nullable
    protected CachedArtifactInfo internalResolve(String host, Artifact artifact) throws IOException {
        if (Objects.equals(artifact, LibraryTransformers.LEGACY_FIXER)) { //TODO: THIS IS A STOP GAP MEASURE
            return CachedArtifactInfo.of("6f09db7f7a4d6241bbd9556556b03ea664ddf567",6171, "");
        }

        if (!KNOWN_LIBRARIES.containsKey(artifact)) return null;

        LOGGER.trace("Resolving remote artifact {} from host {}", artifact, host);
        return Maven.calculateSHA1andSize(KNOWN_LIBRARIES.get(artifact));
    }

}
