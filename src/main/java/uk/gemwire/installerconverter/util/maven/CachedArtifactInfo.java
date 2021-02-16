package uk.gemwire.installerconverter.util.maven;

public record CachedArtifactInfo(String sha1Hash, long expectedSize) {
}
