package uk.gemwire.installerconverter.util.manifest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gemwire.installerconverter.util.Caching;
import uk.gemwire.installerconverter.util.IO;
import uk.gemwire.installerconverter.util.Installers;
import uk.gemwire.installerconverter.util.Jackson;
import uk.gemwire.installerconverter.util.common.Lazy;
import uk.gemwire.installerconverter.util.maven.Maven;

public abstract class VersionManifest {

    private static final Logger LOGGER = LoggerFactory.getLogger(Installers.class);
    private static final String VERSION_MANIFEST = "https://launchermeta.mojang.com/mc/game/version_manifest.json";
    private static final String VERSION_MANIFEST_V2 = "https://launchermeta.mojang.com/mc/game/version_manifest_v2.json";

    //==================================================================================================================

    public static void main(String[] args) throws Exception {
        ObjectNode manifest = Jackson.read(Files.readString(cachedVersionManifestV2()));

        Predicate<VersionInfo> RELEASES  = version -> Objects.equals(version.type(), "release");
        Predicate<VersionInfo> OLD_BETA  = version -> Objects.equals(version.type(), "old_beta");
        Predicate<VersionInfo> OLD_ALPHA = version -> Objects.equals(version.type(), "old_alpha");

        //System.out.println(getVersionInfos(manifest.withArray("versions"))
        //    .stream()
        //    .filter(OLD_BETA)
        //    .map(VersionInfo::id).count());
        //if (true) return;

        List<String> versions =
            getVersionInfos(manifest.withArray("versions"))
                .stream()
                .filter(filterById("1.5.2"))
                .map(VersionInfo::id)
                .sorted()
                .collect(Collectors.toList());

        listAllLibraries(versions);

        //listUsages(versions, "argo:argo:2.25_fixed");
        //listUsages(versions, "org.bouncycastle:bcprov-jdk15on:1.47");
    }

    public static void listAllLibraries(List<String> versions) throws IOException {
        Set<String> allLibraries = new HashSet<>();

        for (String version : versions) {
            allLibraries.addAll(provideLibraries(Jackson.read(Files.readString(VersionManifest.provide(version)))));
        }

        allLibraries.stream().sorted().forEach(System.out::println);
    }

    public static void listUsages(List<String> versions, String artifact) throws IOException {
        for (String version : versions) {
            List<String> libraries = provideLibraries(Jackson.read(Files.readString(VersionManifest.provide(version))));

            if (libraries.contains(artifact))
                System.out.println("Version " + version + " contains " + artifact);
        }
    }

    private static Path cachedVersionManifest() {
        return cachedVersionManifest("version-manifest", VERSION_MANIFEST);
    }

    private static Path cachedVersionManifestV2() {
        return cachedVersionManifest("version-manifest-v2", VERSION_MANIFEST_V2);
    }

    private static Path cachedVersionManifest(String name, String url) {
        return Caching.cached(name + ".json", (destination) -> {
            LOGGER.info("Downloading " + name + " {}...", destination);
            try (InputStream in = Maven.download(new URL(url))) {
                Files.copy(in, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        });
    }

    //==================================================================================================================

    private static final Lazy<String> versionManifest = Lazy.of(VersionManifest::getVersionManifest);

    public static String provideDownload(String version, String side) throws IOException {
        try {
            ObjectNode node = Jackson.read(Files.readString(provide(version)));
            return node.with("downloads").with(side).get("url").asText();
        } catch (NullPointerException e) {
            throw new IOException(e);
        }
    }

    public static List<String> provideLibraries(String version) throws IOException {
        ObjectNode node = Jackson.read(Files.readString(provide(version)));
        return provideLibraries(node);
    }

    public static List<String> provideLibraries(ObjectNode node) {
        List<String> results = new ArrayList<>();

        ArrayNode libraries = node.withArray("libraries");

        libraries.forEach(element -> {
            if (!element.isObject()) throw new IllegalStateException("Element is not an Object");

            ObjectNode library = (ObjectNode) element;
            results.add(library.get("name").asText());
        });

        return results;
    }

    public static Path provide(String version) {
        return Caching.cached("version-info-base/{version}.json".replace("{version}", version), (path) -> download(version, path));
    }

    private static void download(String version, Path destination) throws IOException {
        Files.createDirectories(destination.getParent());

        LOGGER.info("Downloading launcher-meta {} to {}...", version, destination);
        try (InputStream in = Maven.download(new URL(getVersionUrl(version)))) {
            Files.copy(in, destination, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static String getVersionUrl(String version) throws IOException {
        ObjectNode manifest = Jackson.read(versionManifest.get());

        return findVersion(manifest, version).orElseThrow(() -> new IllegalStateException("Could not find " + version + " in Minecraft Version manifest"));
    }

    private static String getVersionManifest() throws IOException {
        LOGGER.info("Downloading version-manifest");
        try (InputStream in = Maven.download(new URL(VERSION_MANIFEST))) {
            return IO.toString(in);
        }
    }

    private static Optional<String> findVersion(ObjectNode manifest, String version) {
        List<VersionInfo> versions = getVersionInfos(manifest.withArray("versions"));
        Predicate<VersionInfo> filter = filterById(version);
        return versions.stream().filter(filter).findFirst().map(VersionInfo::url);
    }

    private static List<VersionInfo> getVersionInfos(ArrayNode array) {
        List<VersionInfo> data = new ArrayList<>();
        array.forEach(element -> data.add(Jackson.JSON.convertValue(element, VersionInfo.class)));
        return data;
    }

    private static Predicate<VersionInfo> filterById(String version) {
        return (pair) -> pair.id().toLowerCase(Locale.ROOT).equals(version.toLowerCase(Locale.ROOT));
    }

}
