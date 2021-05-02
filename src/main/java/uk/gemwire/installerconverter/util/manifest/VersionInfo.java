package uk.gemwire.installerconverter.util.manifest;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import lombok.Value;

/**
 * VersionInfo v2 adds a sha1 and complianceLevel
 */
@Value
public class VersionInfo {

    @Nonnull String id;
    @Nonnull String type;
    @Nonnull String url;
    @Nonnull String time;
    @Nonnull String releaseTime;
    @Nullable String sha1;
    @Nullable Integer complianceLevel;

    public boolean isV2() {
        return sha1 != null;
    }

}
