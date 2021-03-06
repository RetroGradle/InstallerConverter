package uk.gemwire.installerconverter.util.maven;

import javax.annotation.Nullable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;

@JsonDeserialize(converter = Artifact.Converter.class)
public record Artifact(String group, String artifact, String version, @Nullable String classifier) {

    public static Artifact of(String gav) {
        String[] parts = gav.split(":", 4);

        if (parts.length < 3) throw new IllegalStateException("Invalid Maven Artifact: " + gav);

        return new Artifact(parts[0], parts[1], parts[2], parts.length > 3 ? parts[3] : null);
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

    public static class Converter extends StdConverter<String, Artifact> {
        @Override
        public Artifact convert(String value) {
            return Artifact.of(value);
        }
    }

}
