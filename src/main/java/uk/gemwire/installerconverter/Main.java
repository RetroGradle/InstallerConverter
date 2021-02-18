package uk.gemwire.installerconverter;

import java.io.IOException;

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

            InstallerConverter.convert(config, "1.5.2-7.8.1.738");
            InstallerConverter.convert(config, "1.6.4-9.11.1.965");
            InstallerConverter.convert(config, "1.12.2-14.23.5.2847");

            long endTime = System.nanoTime();

            System.out.printf("Time taken: %fms \n", (endTime - startTime) * 1e-6);

        } finally {
            config.teardown();
        }
    }
}
