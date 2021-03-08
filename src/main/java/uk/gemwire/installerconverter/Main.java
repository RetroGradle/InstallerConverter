package uk.gemwire.installerconverter;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gemwire.installerconverter.util.Jackson;
import uk.gemwire.installerconverter.util.maven.ArtifactKey;

/**
 * @author RetroGradle
 */
public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    /**
     * TODO: Generating from Scratch
     * TODO: 1.6.1 has 2 Versions
     * TODO: 1.5.1/1.5.0
     * TODO: 1.4.x
     * TODO: 1.3.x
     * TODO: 1.2.x
     * TODO: 1.1.x
     */

    public static void main(String... args) throws IOException {
        Config config = Config //TODO: WIRING
            .withDefaults()
            .withCachingResolver();

        config.setup();
        //debugResolve(config, ArtifactKey.of(Maven.FORGE, "net.minecraftforge:forge:1.5.2-7.8.1.738:client"));
        //debugResolve(config, ArtifactKey.of("org.bouncycastle:bcprov-jdk15on:1.47"));

        Predicate<String> predicate = (value) -> java.util.Objects.equals(value, "1.6.4-9.11.0.879"); // ""1.6.1-8.9.0.749"); // "1.5.2-7.8.1.738"); // "1.6.4-9.11.1.965"); //

        try {
            //InstallerConverter.convert(config, "1.5.2-7.8.1.738");
            //InstallerConverter.convert(config, "1.6.4-9.11.1.965");
            //InstallerConverter.convert(config, "1.7.10-10.13.4.1614-1.7.10");
            //InstallerConverter.convert(config, "1.12.2-14.23.5.2847");

            long startTime = System.nanoTime();
            List<Path> versions = collectVersions(config).stream().sorted().collect(Collectors.toList()); //TODO: Proper Sort
            for (Path path : versions) {
                String version = getVersion(path);

                // Are we interested in this version?
                if (!predicate.test(version)) continue;

                try {
                    LOGGER.info(version + " installer=" + hasInstaller(path) + " zip=" + hasUniversalZip(path) + " jar=" + hasUniversalJar(path));

                    if (hasInstaller(path)) {
                        InstallerConverter.convert(config, asInstaller(path), version);
                        continue;
                    }

                    if (hasUniversalZip(path)) {
                        InstallerConverter.generate(config, asUniversalZip(path), version);
                        continue;
                    }

                    if (hasUniversalJar(path)) {
                        InstallerConverter.generate(config, asUniversalJar(path), version);
                        continue;
                    }

                    throw new IllegalStateException("No Installer or Universal for " + version);
                } catch (Exception e) {
                    LOGGER.error("Failed for version: " + version, e);
                }
            }

            long endTime = System.nanoTime();

            LOGGER.info("Time taken: {}ms", (endTime - startTime) * 1e-6);

        } finally {
            config.teardown();
        }
    }

    private static List<Path> collectVersions(Config config) throws IOException {
        Path root  = config.localMaven().resolve("net/minecraftforge/forge");
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(root)) {
            return Lists.newArrayList(stream);
        }
    }

    private static String getVersion(Path path) {
        return path.toFile().getName();
    }

    private static Path asInstaller(Path path) {
        return path.resolve("forge-" + getVersion(path) + "-installer.jar");
    }

    private static Path asUniversalZip(Path path) {
        return path.resolve("forge-" + getVersion(path) + "-universal.zip");
    }

    private static Path asUniversalJar(Path path) {
        return path.resolve("forge-" + getVersion(path) + "-universal.jar");
    }

    private static boolean hasInstaller(Path path) {
        return Files.exists(asInstaller(path));
    }

    private static boolean hasUniversalZip(Path path) {
        return Files.exists(asUniversalZip(path));
    }

    private static boolean hasUniversalJar(Path path) {
        return Files.exists(asUniversalJar(path));
    }

    public static void debugResolve(Config config, ArtifactKey key) throws IOException {
        System.out.println(Jackson.write(key.convert(config, Jackson.factory())));
    }
}
