package uk.gemwire.installerconverter.util.maven;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import lombok.Value;

@Value(staticConstructor = "of")
@JsonSerialize(converter = Artifact.Serializer.class)
@JsonDeserialize(converter = Artifact.Deserializer.class)
public class Artifact {

    @Nonnull String group;
    @Nonnull String artifact;
    @Nonnull String version;
    @Nullable String classifier;

    public static Artifact of(String gav) {
        String[] parts = gav.split(":", 4);

        if (parts.length < 3) throw new IllegalStateException("Invalid Maven Artifact: " + gav);

        return of(parts[0], parts[1], parts[2], parts.length > 3 ? parts[3] : null);
    }

    public static Artifact of(String group, String artifact, String version) {
        return of(group, artifact, version, null);
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
