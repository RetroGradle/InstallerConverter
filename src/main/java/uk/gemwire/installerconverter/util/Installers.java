package uk.gemwire.installerconverter.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import lombok.extern.slf4j.Slf4j;
import uk.gemwire.installerconverter.Config;
import uk.gemwire.installerconverter.util.maven.Artifact;
import uk.gemwire.installerconverter.util.maven.Maven;

@Slf4j(topic = "Installers")
public abstract class Installers {

    public static Path provide(Config config) {
        Artifact installer = Artifact.of("net.minecraftforge:installer:{version}:shrunk".replace("{version}", config.installerVersion()));
        Path local = config.localMaven().resolve(installer.asPath());
        if (Files.exists(local)) return local;

        return Caching.cached("installer-{version}-shrunk.jar".replace("{version}", config.installerVersion()), (path) -> download(config, installer, path));
    }

    private static void download(Config config, Artifact artifact, Path destination) throws IOException {
        log.info("Downloading installer {} from {} to {}...", artifact, config.baseMaven() + artifact.asPath(), destination);
        //TODO: MIGRATE TO AN OFFICIAL FORGE BUILD
        try (InputStream in = new URL(Maven.ATERANIMAVIS + artifact.asPath()).openStream()) {
            Files.copy(in, destination, StandardCopyOption.REPLACE_EXISTING);
        }
        log.info("Downloaded installer!");
    }

}
