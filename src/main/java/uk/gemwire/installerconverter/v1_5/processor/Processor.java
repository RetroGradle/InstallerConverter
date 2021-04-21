package uk.gemwire.installerconverter.v1_5.processor;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.google.common.base.MoreObjects;
import uk.gemwire.installerconverter.Config;
import uk.gemwire.installerconverter.util.maven.Artifact;
import uk.gemwire.installerconverter.util.maven.ArtifactKey;
import uk.gemwire.installerconverter.util.maven.Maven;
import uk.gemwire.installerconverter.v1_5.LibraryInfo;

public class Processor {

    private final ArtifactKey processor;
    private final ArtifactKey[] classpathInfos;
    private final Artifact[] classpathArtifacts;

    public Processor(ArtifactKey processor, ArtifactKey[] classpathInfos, Artifact[] classpathArtifacts) {
        this.processor = processor;
        this.classpathInfos = classpathInfos;
        this.classpathArtifacts = classpathArtifacts;
    }

    public ArtifactKey processor() {
        return processor;
    }

    public ArtifactKey[] classpathInfos() {
        return classpathInfos;
    }

    public Artifact[] classpathArtifacts() {
        return classpathArtifacts;
    }

    public Artifact jar() {
        return processor.artifact();
    }

    public Artifact[] classpath() {
        return classpathArtifacts;
    }

    public void injectLibraries(ArrayNode libraries, Config config, JsonNodeFactory factory) throws IOException {
        inject(processor, libraries, config, factory);

        for (ArtifactKey info : classpathInfos)
            inject(info, libraries, config, factory);
    }

    public static void inject(ArtifactKey key, ArrayNode libraries, Config config, JsonNodeFactory factory) throws IOException {
        //TODO: Check that info isn't already in libraries
        libraries.add(LibraryInfo.wrapArtifact(key.artifact().asStringWithClassifier(), key, config, factory));
    }

    public static Processor of(String jar, ArtifactKey... classpath) {
        return of(ArtifactKey.of(Maven.FORGE, jar), classpath);
    }

    public static Processor of(Artifact jar, ArtifactKey... classpath) {
        return of(ArtifactKey.of(Maven.FORGE, jar), classpath);
    }

    public static Processor of(String maven, String jar, ArtifactKey... classpath) {
        return of(ArtifactKey.of(maven, jar), classpath);
    }

    public static Processor of(String maven, Artifact jar, ArtifactKey... classpath) {
        return of(ArtifactKey.of(maven, jar), classpath);
    }

    public static Processor of(ArtifactKey key, ArtifactKey... classpath) {
        return new Processor(key, classpath, Arrays.stream(classpath).map(ArtifactKey::artifact).toArray(Artifact[]::new));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Processor processor1 = (Processor) o;
        return Objects.equals(processor, processor1.processor) && Arrays.equals(classpathInfos, processor1.classpathInfos) && Arrays.equals(classpathArtifacts, processor1.classpathArtifacts);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(processor);
        result = 31 * result + Arrays.hashCode(classpathInfos);
        result = 31 * result + Arrays.hashCode(classpathArtifacts);
        return result;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("processor", processor)
            .add("classpathInfos", classpathInfos)
            .add("classpathArtifacts", classpathArtifacts)
            .toString();
    }
}
