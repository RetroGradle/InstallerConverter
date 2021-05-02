package uk.gemwire.installerconverter.resolver;

import java.io.IOException;
import javax.annotation.Nullable;

import lombok.extern.slf4j.Slf4j;
import uk.gemwire.installerconverter.util.maven.Artifact;
import uk.gemwire.installerconverter.util.maven.CachedArtifactInfo;
import uk.gemwire.installerconverter.util.maven.Maven;

@Slf4j(topic = "RemoteResolver")
public class RemoteResolver extends AbstractResolver {

    public RemoteResolver(@Nullable IResolver fallback) {
        super(fallback, log);
    }

    @Override
    @Nullable
    protected CachedArtifactInfo internalResolve(String host, Artifact artifact) throws IOException {
        log.trace("Resolving remote artifact {} from host {}", artifact, host);
        return Maven.calculateSHA1andSize(host + artifact.asPath());
    }

}
