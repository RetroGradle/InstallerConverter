package uk.gemwire.installerconverter.v1_5.conversion;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import lombok.Value;
import uk.gemwire.installerconverter.Config;
import uk.gemwire.installerconverter.util.maven.CachedArtifactInfo;

@Value(staticConstructor = "of")
public class CommonContext { //TODO: Change from CachedArtifactInfo?

    @Nonnull Config config;
    @Nonnull String minecraft;
    @Nullable CachedArtifactInfo client;
    @Nullable CachedArtifactInfo server;

    public static CommonContext of(Config config, String minecraft) {
        return of(config, minecraft, null, null);
    }

}
