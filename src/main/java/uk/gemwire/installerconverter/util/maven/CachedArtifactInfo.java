package uk.gemwire.installerconverter.util.maven;

import javax.annotation.Nonnull;

import lombok.Value;

@Value(staticConstructor = "of")
public class CachedArtifactInfo {

    @Nonnull String sha1Hash;
    long expectedSize;
    @Nonnull String url;

}
