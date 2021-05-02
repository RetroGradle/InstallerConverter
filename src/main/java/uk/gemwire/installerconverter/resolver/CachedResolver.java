package uk.gemwire.installerconverter.resolver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

import lombok.extern.slf4j.Slf4j;
import uk.gemwire.installerconverter.util.maven.Artifact;
import uk.gemwire.installerconverter.util.maven.ArtifactKey;
import uk.gemwire.installerconverter.util.maven.CachedArtifactInfo;

@Slf4j(topic = "CachedResolver")
public class CachedResolver implements IResolver {

    private final Map<ArtifactKey, CachedArtifactInfo> cache = new HashMap<>();
    private final IResolver fallback;

    public CachedResolver(IResolver fallback) {
        this.fallback = fallback;
    }

    @Override
    @Nullable
    public CachedArtifactInfo resolve(String host, Artifact artifact) {
        log.trace("Retrieving artifact {} (of host {}) from cache", artifact.asStringWithClassifier(), host);
        return cache.computeIfAbsent(ArtifactKey.of(host, artifact), pair -> {
            log.trace("Artifact {} not found in cache, resolving from host {}", pair.artifact().asStringWithClassifier(), pair.host());
            return fallback.resolve(pair.host(), pair.artifact());
        });
    }

    @Override
    public void serialize(Writer writer) throws IOException {
        for (Map.Entry<ArtifactKey, CachedArtifactInfo> entry : cache.entrySet()) {
            ArtifactKey k = entry.getKey();
            String host = k.host();
            String artifact = k.artifact().asStringWithClassifier();

            if (artifact.startsWith("uk.gemwire")) continue; // We don't cache in development

            CachedArtifactInfo v = entry.getValue();
            String sha1 = v.sha1Hash();
            long expectedSize = v.expectedSize();
            String url = v.url();

            writer
                .append(host)
                .append(",")
                .append(artifact)
                .append(",")
                .append(sha1)
                .append(",")
                .append(String.valueOf(expectedSize))
                .append(",")
                .append(url.isEmpty() ? " " : url)
                .append("\r\n");
        }

        writer.flush();
    }

    @Override
    public void deserialize(Reader reader) throws IOException {
        cache.clear();

        try (BufferedReader buffered = new BufferedReader(reader)) {
            buffered
                .lines()
                .forEach(entry -> {
                    String[] parts = entry.split(",");

                    if (parts.length != 5) {
                        log.error(String.format("Invalid cache line (Length '%s' expected 5) '%s'", parts.length, entry));
                        return;
                    }

                    cache.put(ArtifactKey.of(parts[0], Artifact.of(parts[1])), CachedArtifactInfo.of(parts[2], Long.parseLong(parts[3]), parts[4].trim()));
                });
        }
    }

}
