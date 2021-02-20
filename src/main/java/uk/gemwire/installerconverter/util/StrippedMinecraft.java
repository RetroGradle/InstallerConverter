package uk.gemwire.installerconverter.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import javax.annotation.Nullable;

import uk.gemwire.installerconverter.util.manifest.VersionManifest;
import uk.gemwire.installerconverter.util.maven.Artifact;
import uk.gemwire.installerconverter.util.maven.CachedArtifactInfo;
import uk.gemwire.installerconverter.util.maven.Maven;
import uk.gemwire.installertools.processor.ZipProcessor;

public class StrippedMinecraft {

    @Nullable
    public static CachedArtifactInfo provide(Artifact artifact) throws IOException {
        String side = artifact.artifact();
        String version = artifact.version();

        if (!Objects.equals(side, "client") && !Objects.equals(side, "server")) return null;

        try (InputStream stream = Files.newInputStream(provide(side, version))) {
            return Hashing.calculateSHA1andSize(stream, "");
        }
    }

    public static Path provide(String side, String version) {
        return Caching.cached("jars/{side}-{version}-stripped.jar".replace("{side}", side).replace("{version}", version), (path) -> provide(side, version, path));
    }

    public static void provide(String side, String version, Path destination) throws IOException {
        Files.createDirectories(destination.getParent());

        String download = VersionManifest.provideDownload(version, side);

        try (InputStream input = Maven.download(new URL(download)); OutputStream output = Files.newOutputStream(destination)) {
            ZipProcessor.process(input, output);
        }
    }

}
