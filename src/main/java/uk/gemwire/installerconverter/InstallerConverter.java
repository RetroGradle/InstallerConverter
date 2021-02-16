package uk.gemwire.installerconverter;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import uk.gemwire.installerconverter.util.Installers;
import uk.gemwire.installerconverter.util.maven.Artifact;

import static java.nio.file.FileSystems.newFileSystem;

public class InstallerConverter {

    private static final String[] STRAIGHT_COPY = new String[] {
        "CREDITS.txt", "LICENSE.txt", "LICENSE-Paulscode IBXM Library.txt", "LICENSE-Paulscode SoundSystem CodecIBXM.txt"
    };

    public static void convert(String version) throws IOException {
        convert(Config.LOCAL_MAVEN.resolve(Artifact.of("net.minecraftforge:forge:{version}:installer".replace("{version}", version)).asPath()), version);
    }

    public static void convert(Path inputInstaller, String version) throws IOException {
        System.out.println("Converting Installer for version " + version);

        // The main in-memory FileSystem (Jimfs)
        FileSystem inMemFS = Jimfs.newFileSystem("installerconverter", Configuration.unix());

        // Copy the Installer to memory
        System.out.println(" - Copying Installer to memory");
        Path inMemInstaller = inMemFS.getPath(inputInstaller.getFileName().toString());
        Files.copy(inputInstaller, inMemInstaller);

        // Get Installer Base (downloaded and/or cached)
        System.out.println(" - Retrieving Installer Base");
        Path installerBase = Installers.provide(Config.INSTALLER_VERSION);

        // Get the path to the in-memory output jar
        System.out.println(" - Copying Installer Base to memory");
        Path memoryOutputJar = inMemFS.getPath("output.jar");
        Files.copy(installerBase, memoryOutputJar);

        // Open up the two jars (Installer and the output) in-memory
        System.out.println(" - Loading in-memory input installer and output jars");
        try (FileSystem installer = newFileSystem(inMemInstaller);
             FileSystem output = newFileSystem(memoryOutputJar)) {

            // Validate the Base Installer is not already in the new format
            if (Files.exists(installer.getPath("maven"))) {
                System.out.println(" - Installer already in 2.0 Format skipping");
                return;
            }

            // Convert `installer_profile.json` -> `installer_profile.json` & `version.json`
            System.out.println(" - Converting installer profile");
            convertProfile(
                installer.getPath("installer_profile.json"),
                output.getPath("installer_profile.json"),
                output.getPath("version.json")
            );

            // Copy `forge-{version}-universal.jar` to `maven/net/minecraftforge/forge/{version}/forge-{version}.jar`
            System.out.println(" - Copying universal jar");
            Files.createDirectories(output.getPath("maven/net/minecraftforge/forge/{version}".replace("{version}", version)));
            Files.copy(installer.getPath("forge-{version}-universal.jar".replace("{version}", version)), output.getPath("maven/net/minecraftforge/forge/{version}/forge-{version}.jar".replace("{version}", version)));


            // Optionally Copy Big Logo
            if (Config.OVERRIDE_INSTALLER_BIG_LOGO) {
                System.out.println(" - Copying big_logo.png");
                copy(installer, output, "big_logo.png");
            }

            // Copy Files
            System.out.println(" - Copying files");
            for (String file : STRAIGHT_COPY) copy(installer, output, file); //TODO: Instead filter for .txt's missing changelog.txt?
        }
        // (FSs are closed here; important for the output.jar so the contents are written)
        // Copy the resulting jar
        System.out.println(" - Copying output zip to disk");
        Files.copy(memoryOutputJar, Path.of("output.jar"), StandardCopyOption.REPLACE_EXISTING); //TODO: Location
        inMemFS.close();

        //TODO: The 2.0 Installer Jars should probably be signed

        System.out.printf("Conversion of Installer for version %s is complete.%n", version);
    }

    private static void convertProfile(Path original, Path converted, Path versionInfo) {
        //TODO:
    }

    private static void copy(FileSystem input, FileSystem output, String path) throws IOException {
        Files.copy(input.getPath(path), output.getPath(path), StandardCopyOption.REPLACE_EXISTING);
    }

}
