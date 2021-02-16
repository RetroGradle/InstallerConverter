package uk.gemwire.installerconverter.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public interface Caching {

    Path CACHE = Path.of(".cache");

    static Path cached(String path, IOConsumer<Path> generator) throws CachingException {
        try {
            Files.createDirectories(CACHE);
        } catch (IOException exception) {
            throw new CachingException("Failed to create parent directories for cached file " + path, exception);
        }
        return cached(CACHE.resolve(path), generator);
    }

    static Path cached(Path cached, IOConsumer<Path> generator) throws CachingException {
        if (Files.exists(cached)) return cached;

        System.out.printf("Caching: no cached copy of %s, running generator%n", cached);
        try {
            generator.accept(cached);
        } catch (IOException exception) {
            throw new CachingException("Failed to generate file " + cached, exception);
        }

        if (!Files.exists(cached)) throw new CachingException("Generator did not generate file" + cached);

        return cached;
    }

    class CachingException extends RuntimeException {
        public CachingException(String message) {
            super(message);
        }

        public CachingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
