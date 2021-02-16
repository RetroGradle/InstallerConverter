package uk.gemwire.installerconverter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gemwire.installerconverter.util.Installers;
import uk.gemwire.installerconverter.util.Jackson;
import uk.gemwire.installerconverter.util.Pair;
import uk.gemwire.installerconverter.util.maven.Artifact;
import uk.gemwire.installerconverter.v1_5.InstallProfile;

import static java.nio.file.FileSystems.newFileSystem;

public class InstallerConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(InstallerConverter.class);
    
    private static final String[] STRAIGHT_COPY = new String[] {
        "CREDITS.txt", "LICENSE.txt", "LICENSE-Paulscode IBXM Library.txt", "LICENSE-Paulscode SoundSystem CodecIBXM.txt"
    };

    public static void convert(String version) throws IOException {
        convert(Config.LOCAL_MAVEN.resolve(Artifact.of("net.minecraftforge:forge:{version}:installer".replace("{version}", version)).asPath()), version);
    }

    public static void convert(Path inputInstaller, String version) throws IOException {
        LOGGER.info("Converting Installer for version " + version);

        // The main in-memory FileSystem (Jimfs)
        FileSystem inMemFS = Jimfs.newFileSystem("installerconverter", Configuration.unix());

        // Copy the Installer to memory
        LOGGER.info(" - Copying installer JAR to memory");
        Path inMemInstaller = inMemFS.getPath(inputInstaller.getFileName().toString());
        Files.copy(inputInstaller, inMemInstaller);

        // Get Installer Base (downloaded and/or cached)
        LOGGER.info(" - Retrieving base installer");
        Path installerBase = Installers.provide(Config.INSTALLER_VERSION);

        // Get the path to the in-memory output jar
        LOGGER.info(" - Copying base installer to memory");
        Path memoryOutputJar = inMemFS.getPath("output.jar");
        Files.copy(installerBase, memoryOutputJar);

        // Open up the two jars (Installer and the output) in-memory
        LOGGER.info(" - Loading in-memory input installer and output jars");
        try (FileSystem installer = newFileSystem(inMemInstaller);
             FileSystem output = newFileSystem(memoryOutputJar)) {

            // Validate the Base Installer is not already in the new format
            if (Files.exists(installer.getPath("maven"))) {
                LOGGER.info("Installer is already version 2.0, skipping...");
                return;
            }

            // Convert `install_profile.json` -> `install_profile.json` & `version.json`
            LOGGER.info(" - Converting install profile");
            convertProfile(
                installer.getPath("install_profile.json"),
                output.getPath("install_profile.json"),
                output.getPath("version.json")
            );

            // Copy `forge-{version}-universal.jar` to `maven/net/minecraftforge/forge/{version}/forge-{version}.jar`
            LOGGER.info(" - Copying universal jar");
            Files.createDirectories(output.getPath("maven/net/minecraftforge/forge/{version}".replace("{version}", version)));
            Files.copy(installer.getPath("forge-{version}-universal.jar".replace("{version}", version)), output.getPath("maven/net/minecraftforge/forge/{version}/forge-{version}.jar".replace("{version}", version)));

            // Optionally Copy Big Logo
            if (Config.OVERRIDE_INSTALLER_BIG_LOGO) {
                LOGGER.info(" - Copying big_logo.png");
                copy(installer, output, "big_logo.png");
            }

            // Copy Files
            LOGGER.info(" - Copying other files");
            for (String file : STRAIGHT_COPY) copy(installer, output, file); //TODO: Instead filter for .txt's missing changelog.txt?
        }
        // (FSs are closed here; important for the output.jar so the contents are written)
        // Copy the resulting jar
        LOGGER.info(" - Copying output zip to disk");
        Files.copy(memoryOutputJar, Path.of("output.jar"), StandardCopyOption.REPLACE_EXISTING); //TODO: Location
        inMemFS.close();

        //TODO: The 2.0 Installer Jars should probably be signed

        LOGGER.info("Conversion of Installer for version {} is complete.", version);
    }

    private static void convertProfile(Path original, Path converted, Path versionInfo) throws IOException {
        try (InputStream stream = Files.newInputStream(original)) {
            InstallProfile profile = Jackson.JSON.readValue(stream, InstallProfile.class);

            profile.validate();

            Pair<ObjectNode, ObjectNode> modified = profile.convert(Jackson.JSON.getNodeFactory());

            LOGGER.info(" - Writing install-profile.json");
            try (OutputStream out = Files.newOutputStream(converted)) {
                Jackson.JSON.writeValue(out, modified.left());
            }

            LOGGER.info(" - Writing version.json");
            try (OutputStream out = Files.newOutputStream(versionInfo)) {
                Jackson.JSON.writeValue(out, modified.right());
            }
        }
    }

    private static void copy(FileSystem input, FileSystem output, String path) throws IOException {
        Files.copy(input.getPath(path), output.getPath(path), StandardCopyOption.REPLACE_EXISTING);
    }

}
