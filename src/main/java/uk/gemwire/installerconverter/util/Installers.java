package uk.gemwire.installerconverter.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gemwire.installerconverter.Config;
import uk.gemwire.installerconverter.util.maven.Artifact;
import uk.gemwire.installerconverter.util.maven.Maven;

public class Installers {
    private static final Logger LOGGER = LoggerFactory.getLogger(Installers.class);

    public static Path provide(Config config) {
        Artifact installer = Artifact.of("net.minecraftforge:installer:{version}:shrunk".replace("{version}", config.installerVersion()));
        Path local = config.localMaven().resolve(installer.asPath());
        if (Files.exists(local)) return local;

        return Caching.cached("installer-{version}-shrunk.jar".replace("{version}", config.installerVersion()), (path) -> download(config, installer, path));
    }

    private static void download(Config config, Artifact artifact, Path destination) throws IOException {
        LOGGER.info("Downloading installer {} from {} to {}...", artifact, config.baseMaven() + artifact.asPath(), destination);
        //TODO: MIGRATE TO AN OFFICIAL FORGE BUILD
        try (InputStream in = new URL(Maven.ATERANIMAVIS + artifact.asPath()).openStream()) {
            Files.copy(in, destination, StandardCopyOption.REPLACE_EXISTING);
        }
        LOGGER.info("Downloaded installer!");
    }

}
