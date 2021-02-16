package uk.gemwire.installerconverter.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import uk.gemwire.installerconverter.Config;
import uk.gemwire.installerconverter.util.maven.Artifact;

import static uk.gemwire.installerconverter.Config.BASE_MAVEN;

public class Installers {

    public static Path provide(String installerVersion) throws IOException {
        Artifact installer = Artifact.of("net.minecraftforge:installer:{version}:shrunk".replace("{version}", installerVersion));
        Path local = Config.LOCAL_MAVEN.resolve(installer.asPath());
        if (Files.exists(local)) return local;

        return Caching.cached("installer-{version}-shrunk.jar".replace("{version}", installerVersion), (path) -> download(installer, path));
    }

    private static void download(Artifact artifact, Path destination) throws IOException {
        System.out.printf("InstallerConverter: downloading %s...", BASE_MAVEN + artifact.asPath());
        try (InputStream in = new URL(BASE_MAVEN + artifact.asPath()).openStream()) {
            Files.copy(in, destination, StandardCopyOption.REPLACE_EXISTING);
        }
        System.out.println(" done!");
    }

}
