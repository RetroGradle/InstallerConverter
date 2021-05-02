package uk.gemwire.installerconverter.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import lombok.extern.slf4j.Slf4j;
import uk.gemwire.installerconverter.util.common.IOConsumer;
import uk.gemwire.installerconverter.util.exception.CachingException;

@Slf4j(topic = "Caching")
public abstract class Caching {
    private static final Path CACHE = Path.of(".cache");

    public static Path cached(String path, IOConsumer<Path> generator) throws CachingException {
        try {
            Files.createDirectories(CACHE);
        } catch (IOException exception) {
            throw new CachingException("Failed to create parent directories for cached file " + path, exception);
        }
        return cached(CACHE.resolve(path), generator);
    }

    public static Path cached(Path cached, IOConsumer<Path> generator) throws CachingException {
        if (Files.exists(cached)) return cached;

        log.trace("Caching: no cached copy of {}, running generator", cached);

        try {
            generator.accept(cached);
        } catch (IOException exception) {
            throw new CachingException("Failed to generate file " + cached, exception);
        }

        if (!Files.exists(cached)) throw new CachingException("Generator did not generate file" + cached);

        return cached;
    }
}
