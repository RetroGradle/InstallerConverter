package uk.gemwire.installerconverter.v1_5.processor;

import java.io.IOException;
import java.util.Arrays;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import uk.gemwire.installerconverter.Config;
import uk.gemwire.installerconverter.util.maven.Artifact;
import uk.gemwire.installerconverter.util.maven.ArtifactKey;
import uk.gemwire.installerconverter.util.maven.Maven;
import uk.gemwire.installerconverter.v1_5.LibraryInfo;

public record Processor(ArtifactKey processor, ArtifactKey[] classpathInfos, Artifact[] classpathArtifacts) {

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

}
