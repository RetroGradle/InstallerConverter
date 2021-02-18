package uk.gemwire.installerconverter;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.Function;

import uk.gemwire.installerconverter.resolver.CachedResolver;
import uk.gemwire.installerconverter.resolver.IResolver;
import uk.gemwire.installerconverter.resolver.LocalResolver;
import uk.gemwire.installerconverter.resolver.RemoteResolver;

/**
 * @author RetroGradle
 */
public class Main {

    public static void main(String... args) throws IOException {
        Config config = Config //TODO: WIRING
            .withDefaults()
            .withCachingResolver();

        config.setup();

        try {
            long startTime = System.nanoTime();

            InstallerConverter.convert(config, "1.12.2-14.23.5.2847");

            long endTime = System.nanoTime();

            System.out.printf("Time taken: %fms \n", (endTime - startTime) * 1e-6);

        } finally {
            config.teardown();
        }
    }
}
