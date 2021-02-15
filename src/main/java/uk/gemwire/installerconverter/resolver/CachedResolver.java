package uk.gemwire.installerconverter.resolver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

import uk.gemwire.installerconverter.util.Log;
import uk.gemwire.installerconverter.util.Pair;
import uk.gemwire.installerconverter.util.maven.Artifact;

public class CachedResolver implements IResolver {

    private final Map<Pair<String, Artifact>, Pair<String, Long>> cache = new HashMap<>();
    private final IResolver fallback;

    public CachedResolver(IResolver fallback) {
        this.fallback = fallback;
    }

    @Override
    @Nullable
    public Pair<String, Long> resolve(String host, Artifact artifact) {
        Log.trace(String.format("Resolving (Cached?): '%s' from '%s'", artifact.asStringWithClassifier(), host));
        return cache.computeIfAbsent(Pair.of(host, artifact), pair -> {
            Log.trace(String.format("Not Cached hitting actual: '%s' from '%s'", artifact.asStringWithClassifier(), host));
            return fallback.resolve(pair.left(), pair.right());
        });
    }

    public void serialize(Writer writer) throws IOException {
        for (Map.Entry<Pair<String, Artifact>, Pair<String, Long>> entry : cache.entrySet()) {
            Pair<String, Artifact> k = entry.getKey();
            String host = k.left();
            String artifact = k.right().asStringWithClassifier();

            Pair<String, Long> v = entry.getValue();
            String sha1 = v.left();
            long expectedSize = v.right();

            writer
                .append(host)
                .append(",")
                .append(artifact)
                .append(",")
                .append(sha1)
                .append(",")
                .append(String.valueOf(expectedSize))
                .append("\r\n");
        }

        writer.flush();
    }

    public void deserialize(Reader reader) throws IOException {
        cache.clear();

        try (BufferedReader buffered = new BufferedReader(reader)) {
            buffered
                .lines()
                .forEach(entry -> {
                    String[] parts = entry.split(",", 4);

                    if (parts.length != 4) {
                        Log.error(String.format("Invalid cache line '%s'", entry));
                        return;
                    }

                    cache.put(Pair.of(parts[0], Artifact.of(parts[1])), Pair.of(parts[2], Long.valueOf(parts[3])));
                });
        }

    }

}
