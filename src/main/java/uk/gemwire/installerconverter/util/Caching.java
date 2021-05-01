package uk.gemwire.installerconverter.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gemwire.installerconverter.util.common.IOConsumer;
import uk.gemwire.installerconverter.util.exception.CachingException;

public abstract class Caching {
    private static final Logger LOGGER = LoggerFactory.getLogger("Caching");

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

        LOGGER.debug("Caching: no cached copy of {}, running generator", cached);

        try {
            generator.accept(cached);
        } catch (IOException exception) {
            throw new CachingException("Failed to generate file " + cached, exception);
        }

        if (!Files.exists(cached)) throw new CachingException("Generator did not generate file" + cached);

        return cached;
    }
}
