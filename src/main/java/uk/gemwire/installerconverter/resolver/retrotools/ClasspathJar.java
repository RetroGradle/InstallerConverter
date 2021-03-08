package uk.gemwire.installerconverter.resolver.retrotools;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import uk.gemwire.installerconverter.util.Caching;
import uk.gemwire.installerconverter.util.Hashing;
import uk.gemwire.installerconverter.util.maven.CachedArtifactInfo;
import uk.gemwire.installertools.Main;

public class ClasspathJar {

    public static CachedArtifactInfo provide(String version, String[] arguments) throws IOException { //TODO: Caching
        try (InputStream stream = Files.newInputStream(provideJar(version, arguments))) {
            return Hashing.calculateSHA1andSize(stream, "");
        }
    }

    public static Path provideJar(String version, String[] arguments) {
        return Caching.cached("jars/{version}-classpath.jar".replace("{version}", version), (path) -> provide(arguments, path));
    }

    public static void provide(String[] arguments, Path destination) throws IOException {
        Files.createDirectories(destination.getParent());

        Main.main(
            Arrays.stream(arguments)
            .map(a -> a.replace("{CLASSPATH}", destination.toString()))
            .toArray(String[]::new)
        );
    }

}
