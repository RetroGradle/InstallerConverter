package uk.gemwire.installerconverter.util.manifest;

import javax.annotation.Nullable;

/**
 * VersionInfo v2 adds a sha1 and complianceLevel
 */
public record VersionInfo(String id, String type, String url, String time, String releaseTime, @Nullable String sha1, @Nullable Integer complianceLevel) {

    public boolean isV2() {
        return sha1 != null;
    }

}
