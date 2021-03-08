package uk.gemwire.installerconverter.v1_5.conversion;

import uk.gemwire.installerconverter.Config;
import uk.gemwire.installerconverter.util.maven.CachedArtifactInfo;

public record CommonContext(Config config, String minecraft, CachedArtifactInfo client, CachedArtifactInfo server) { //TODO: Change from CachedArtifactInfo?

    public static CommonContext of(Config config, String minecraft) {
        return new CommonContext(config, minecraft, null, null);
    }

    public static CommonContext of(Config config, String minecraft, CachedArtifactInfo client, CachedArtifactInfo server) {
        return new CommonContext(config, minecraft, client, server);
    }

}
