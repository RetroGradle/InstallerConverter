package uk.gemwire.installerconverter.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gemwire.installerconverter.util.maven.Maven;

public abstract class VersionManifest {

    private static final Logger LOGGER = LoggerFactory.getLogger(Installers.class);
    private static final String VERSION_MANIFEST = "https://launchermeta.mojang.com/mc/game/version_manifest.json";

    public static List<String> provideLibraries(String version) throws IOException {
        List<String> results = new ArrayList<>();

        ObjectNode node = Jackson.read(Files.readString(provide(version)));
        ArrayNode libraries = node.withArray("libraries");

        libraries.forEach(element -> {
            if (!element.isObject()) throw new IllegalStateException("Element is not an Object");

            ObjectNode library = (ObjectNode) element;
            results.add(library.get("name").asText());
        });

        return results;
    }

    public static Path provide(String version) {
        return Caching.cached("version-info-base-{version}.json".replace("{version}", version), (path) -> download(version, path));
    }

    private static void download(String version, Path destination) throws IOException {
        LOGGER.info("Downloading version-manifest {} to {}...", version, destination);
        try (InputStream in = Maven.download(new URL(getVersionUrl(version)))) {
            Files.copy(in, destination, StandardCopyOption.REPLACE_EXISTING);
        }
        LOGGER.info("Downloaded version-manifest!");
    }

    private static String getVersionUrl(String version) throws IOException {
        ObjectNode manifest = Jackson.read(getVersionManifest());

        return findVersion(manifest, version).orElseThrow(() -> new IllegalStateException("Could not find " + version + " in Minecraft Version manifest"));
    }

    //TODO: CACHE PER RUN
    private static String getVersionManifest() throws IOException {
        try (InputStream in = Maven.download(new URL(VERSION_MANIFEST))) {
            return IO.toString(in);
        }
    }

    private static Optional<String> findVersion(ObjectNode manifest, String version) {
        List<Pair<String, String>> versions = getVersions(manifest.withArray("versions"));
        Predicate<Pair<String, String>> filter = filterById(version);
        return versions.stream().filter(filter).findFirst().map(Pair::right);
    }

    private static List<Pair<String, String>> getVersions(ArrayNode array) {
        List<Pair<String, String>> data = new ArrayList<>();
        array.forEach(element -> {
            if (!element.isObject()) throw new IllegalStateException("Element is not an Object");

            ObjectNode version = (ObjectNode) element;
            data.add(Pair.of(version.get("id").asText(), version.get("url").asText()));
        });
        return data;
    }

    private static Predicate<Pair<String, String>> filterById(String version) {
        return (pair) -> pair.left().toLowerCase(Locale.ROOT).equals(version.toLowerCase(Locale.ROOT));
    }

}
