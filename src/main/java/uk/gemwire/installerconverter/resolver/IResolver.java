package uk.gemwire.installerconverter.resolver;

import javax.annotation.Nullable;

import uk.gemwire.installerconverter.util.maven.Artifact;
import uk.gemwire.installerconverter.util.maven.CachedArtifactInfo;

public interface IResolver {

    @Nullable
    CachedArtifactInfo resolve(String host, Artifact artifact);

}
