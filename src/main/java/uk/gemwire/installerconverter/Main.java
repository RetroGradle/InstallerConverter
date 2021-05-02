package uk.gemwire.installerconverter;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gemwire.installerconverter.util.Jackson;
import uk.gemwire.installerconverter.util.maven.ArtifactKey;
import uk.gemwire.installerconverter.v1_5.conversion.Conversions;

/**
 * @author RetroGradle
 */
public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger("Main");

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
            .withLocalMaven(Path.of(".cache/local"))    // Must be configured before withCachingResolver
            .withCachingResolver()                      // Setups Resolvers so it uses a cache
            .withOutput(Path.of("outputs"))             // Output Location
            .transformSigningConfig(it -> it            // Configure Signing Parameters
                .withKeystore(Path.of("keystore.jks"))  // Path to keystore
                .withStorepass("keystorepass")          // Keystore password
                //.withKeypass("keypass")               // Key password (Can be skipped or pass in "")
                .withAlias("forge")                     // Default Value can be skipped
            );
            //.withSigningConfig(null);                 // Skip Signing by removing the SigningConfig

        config.setup(); //

        /* Testing Versions
            .cache\local\net\minecraftforge\forge\1.5.2-7.8.0.684
            .cache\local\net\minecraftforge\forge\1.5.2-7.8.1.738
            .cache\local\net\minecraftforge\forge\1.6.1-8.9.0.749
            .cache\local\net\minecraftforge\forge\1.6.1-8.9.0.751
            .cache\local\net\minecraftforge\forge\1.6.4-9.11.0.879
            .cache\local\net\minecraftforge\forge\1.6.4-9.11.1.965

            // 1.7.x -> 1.12.x
            .cache\local\net\minecraftforge\forge\1.7.10-10.13.4.1566-1.7.10
            .cache\local\net\minecraftforge\forge\1.7.10-10.13.4.1614-1.7.10
            .cache\local\net\minecraftforge\forge\1.8.9-11.15.1.2318-1.8.9
            .cache\local\net\minecraftforge\forge\1.9.4-12.17.0.2051
            .cache\local\net\minecraftforge\forge\1.10.2-12.18.3.2511
            .cache\local\net\minecraftforge\forge\1.11.2-13.20.1.2588
            .cache\local\net\minecraftforge\forge\1.12.2-14.23.5.2847
            .cache\local\net\minecraftforge\forge\1.12.2-14.23.5.2855
         */

        Predicate<String> predicate = (value) -> {
            String versionString = Conversions.convertVersion(value).split("\\.", 3)[1];
            int version = Integer.parseInt(versionString);
            return 7 <= version && version <= 12; //
        };

        try {
            long startTime = System.nanoTime();
            List<Path> versions = collectVersions(config)
                .stream()
                .sorted(Comparator.comparing(it -> Conversions.asComparableVersion(getVersion(it))))
                .collect(Collectors.toList());

            for (Path path : versions) {
                String version = getVersion(path);

                // Are we interested in this version?
                if (!predicate.test(version)) continue;

                try {
                    LOGGER.trace(version + " installer=" + hasInstaller(path) + " zip=" + hasUniversalZip(path) + " jar=" + hasUniversalJar(path));

                    Path installer = asInstaller(path);
                    if (Files.exists(installer)) {
                        InstallerConverter.convert(config, path, installer, version);
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
