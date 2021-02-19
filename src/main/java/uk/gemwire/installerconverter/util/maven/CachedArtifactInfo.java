package uk.gemwire.installerconverter.util.maven;

public record CachedArtifactInfo(String sha1Hash, long expectedSize, String url) {

    public static CachedArtifactInfo of(String sha1Hash, long expectedSize, String url) {
        return new CachedArtifactInfo(sha1Hash, expectedSize, url);
    }
}
