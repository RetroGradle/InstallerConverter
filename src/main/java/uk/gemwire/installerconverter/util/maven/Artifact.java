package uk.gemwire.installerconverter.util.maven;

import java.util.Objects;
import javax.annotation.Nullable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.util.StdConverter;

@JsonSerialize(converter = Artifact.Serializer.class)
@JsonDeserialize(converter = Artifact.Deserializer.class)
public class Artifact {

    private final String group;
    private final String artifact;
    private final String version;
    @Nullable private final String classifier;

    public Artifact(String group, String artifact, String version, @Nullable String classifier) {
        this.group = group;
        this.artifact = artifact;
        this.version = version;
        this.classifier = classifier;
    }

    public String group() {
        return group;
    }

    public String artifact() {
        return artifact;
    }

    public String version() {
        return version;
    }

    @Nullable
    public String classifier() {
        return classifier;
    }

    public static Artifact of(String gav) {
        String[] parts = gav.split(":", 4);

        if (parts.length < 3) throw new IllegalStateException("Invalid Maven Artifact: " + gav);

        return new Artifact(parts[0], parts[1], parts[2], parts.length > 3 ? parts[3] : null);
    }

    public static Artifact of(String group, String artifact, String version) {
        return new Artifact(group, artifact, version, null);
    }

    public static Artifact of(String group, String artifact, String version, @Nullable String classifier) {
        return new Artifact(group, artifact, version, classifier);
    }

    public String asString() {
        return group + ":" + artifact + ":" + version;
    }

    public String asStringWithClassifier() {
        return asString() + (classifier == null ? "" : ":" + classifier);
    }

    public String asPath() {
        String jar = artifact + "-" + version + (classifier == null ? "" : "-" + classifier) + ".jar";
        return group.replaceAll("\\.", "/") + "/" + artifact + "/" + version + "/" + jar;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Artifact artifact1 = (Artifact) o;
        return Objects.equals(group, artifact1.group) && Objects.equals(artifact, artifact1.artifact) && Objects.equals(version, artifact1.version) && Objects.equals(classifier, artifact1.classifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(group, artifact, version, classifier);
    }

    @Override
    public String toString() {
        return asStringWithClassifier();
    }

    public static class Serializer extends StdConverter<Artifact, String> {
        @Override
        public String convert(Artifact value) {
            return value.asStringWithClassifier();
        }
    }

    public static class Deserializer extends StdConverter<String, Artifact> {
        @Override
        public Artifact convert(String value) {
            return Artifact.of(value);
        }
    }

}
