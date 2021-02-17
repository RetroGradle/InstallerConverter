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
    public static final Path PATH_CACHED_RESOLVER = Path.of("sha1-size.cache");
    public static CachedResolver CACHED_RESOLVER = null;

    public static void main(String... args) throws IOException {
        setup();

        InstallerConverter.convert("1.12.2-14.23.5.2847");
        InstallerConverter.convert("1.12.2-14.23.5.2855");

        teardown();
    }

    public static void setup() throws IOException { //TODO: WIRING
        Config.LOCAL_MAVEN = Path.of("local");

        Function<IResolver, CachedResolver> caching = CachedResolver::new;
        Function<IResolver, IResolver> fromLocalMaven = (f) -> new LocalResolver(Config.LOCAL_MAVEN, f);
        Function<IResolver, IResolver> fromRemote = RemoteResolver::new;

        CACHED_RESOLVER = caching.compose(fromLocalMaven).compose(fromRemote).apply(null);

        if (Files.exists(PATH_CACHED_RESOLVER)) {
            try (Reader reader = Files.newBufferedReader(PATH_CACHED_RESOLVER)) {
                CACHED_RESOLVER.deserialize(reader);
            }
        }

        Config.RESOLVER = CACHED_RESOLVER;
    }

    public static void teardown() throws IOException {
        try (Writer writer = Files.newBufferedWriter(PATH_CACHED_RESOLVER, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            CACHED_RESOLVER.serialize(writer);
        }
    }
}
