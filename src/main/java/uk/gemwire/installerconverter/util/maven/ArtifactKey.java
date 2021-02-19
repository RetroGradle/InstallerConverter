package uk.gemwire.installerconverter.util.maven;

public record ArtifactKey(String host, Artifact artifact) {

    public static ArtifactKey of(String host, Artifact artifact) {
        return new ArtifactKey(host, artifact);
    }

}
