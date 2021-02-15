package uk.gemwire.installerconverter.resolver;

import javax.annotation.Nullable;

import uk.gemwire.installerconverter.util.Pair;
import uk.gemwire.installerconverter.util.maven.Artifact;

public interface IResolver {

    @Nullable
    Pair<String, Long> resolve(String host, Artifact artifact);

}
