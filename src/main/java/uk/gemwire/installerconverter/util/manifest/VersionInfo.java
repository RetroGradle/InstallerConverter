package uk.gemwire.installerconverter.util.manifest;

import java.util.Objects;
import javax.annotation.Nullable;

import com.google.common.base.MoreObjects;

/**
 * VersionInfo v2 adds a sha1 and complianceLevel
 */
public class VersionInfo {

    private final String id;
    private final String type;
    private final String url;
    private final String time;
    private final String releaseTime;
    @Nullable private final String sha1;
    @Nullable private final Integer complianceLevel;

    public VersionInfo(String id, String type, String url, String time, String releaseTime, @Nullable String sha1, @Nullable Integer complianceLevel) {
        this.id = id;
        this.type = type;
        this.url = url;
        this.time = time;
        this.releaseTime = releaseTime;
        this.sha1 = sha1;
        this.complianceLevel = complianceLevel;
    }

    public String id() {
        return id;
    }

    public String type() {
        return type;
    }

    public String url() {
        return url;
    }

    public String time() {
        return time;
    }

    public String releaseTime() {
        return releaseTime;
    }

    @Nullable
    public String sha1() {
        return sha1;
    }

    @Nullable
    public Integer complianceLevel() {
        return complianceLevel;
    }

    public boolean isV2() {
        return sha1 != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VersionInfo that = (VersionInfo) o;
        return Objects.equals(id, that.id) && Objects.equals(type, that.type) && Objects.equals(url, that.url) &&
            Objects.equals(time, that.time) && Objects.equals(releaseTime, that.releaseTime) &&
            Objects.equals(sha1, that.sha1) && Objects.equals(complianceLevel, that.complianceLevel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, url, time, releaseTime, sha1, complianceLevel);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("id", id)
            .add("type", type)
            .add("url", url)
            .add("time", time)
            .add("releaseTime", releaseTime)
            .add("sha1", sha1)
            .add("complianceLevel", complianceLevel)
            .toString();
    }
}
