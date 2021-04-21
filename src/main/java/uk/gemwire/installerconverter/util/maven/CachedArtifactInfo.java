package uk.gemwire.installerconverter.util.maven;

import java.util.Objects;

import com.google.common.base.MoreObjects;

public class CachedArtifactInfo {

    private final String sha1Hash;
    private final long expectedSize;
    private final String url;

    public CachedArtifactInfo(String sha1Hash, long expectedSize, String url) {
        this.sha1Hash = sha1Hash;
        this.expectedSize = expectedSize;
        this.url = url;
    }

    public String sha1Hash() {
        return sha1Hash;
    }

    public long expectedSize() {
        return expectedSize;
    }

    public String url() {
        return url;
    }

    public static CachedArtifactInfo of(String sha1Hash, long expectedSize, String url) {
        return new CachedArtifactInfo(sha1Hash, expectedSize, url);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CachedArtifactInfo that = (CachedArtifactInfo) o;
        return expectedSize == that.expectedSize && Objects.equals(sha1Hash, that.sha1Hash) && Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sha1Hash, expectedSize, url);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("sha1Hash", sha1Hash)
            .add("expectedSize", expectedSize)
            .add("url", url)
            .toString();
    }
}
