package uk.gemwire.installerconverter;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.Function;

import com.fasterxml.jackson.databind.node.ObjectNode;
import uk.gemwire.installerconverter.resolver.CachedResolver;
import uk.gemwire.installerconverter.resolver.IResolver;
import uk.gemwire.installerconverter.resolver.LocalResolver;
import uk.gemwire.installerconverter.resolver.RemoteResolver;
import uk.gemwire.installerconverter.util.Jackson;
import uk.gemwire.installerconverter.util.Pair;
import uk.gemwire.installerconverter.v1_5.InstallProfile;

/**
 * @author RetroGradle
 */
public class Main {
    public static final Path PATH_CACHED_RESOLVER = Path.of("sha1-size.cache");
    public static CachedResolver CACHED_RESOLVER = null;

    public static void main(String... args) throws IOException {
        setup();

        //printObj();
        InstallerConverter.convert("1.12.2-14.23.5.2847");
        InstallerConverter.convert("1.12.2-14.23.5.2855");

        //System.out.println("Old: " + Files.exists(OLD_1_12));
        //System.out.println("New: " + Files.exists(NEW_1_12));
        //
        //System.out.println("Converting org.ow2.asm:asm-all:5.2");
        //
        //LibraryInfo info = Jackson.JSON.readValue(
        //    "{"
        //    + "      \"name\": \"org.ow2.asm:asm-all:5.2\",\n"
        //    + "      \"url\" : \"http://files.minecraftforge.net/maven/\",\n"
        //    + "      \"checksums\" : [ \"2ea49e08b876bbd33e0a7ce75c8f371d29e1f10a\" ],\n"
        //    + "      \"serverreq\":true,\n"
        //    + "      \"clientreq\":true\n"
        //    + "    }",
        //    LibraryInfo.class
        //);
        //
        //info.convert(Jackson.JSON.getNodeFactory());

        teardown();
    }

    public static void setup() throws IOException { //TODO: WIRING
        Config.LOCAL_MAVEN = Path.of("local");

        Function<IResolver, CachedResolver> caching = CachedResolver::new;
        Function<IResolver, IResolver> fromLocalMaven = (f) -> new LocalResolver(Config.LOCAL_MAVEN, f);
        Function<IResolver, IResolver> fromRemote = RemoteResolver::new;

        CACHED_RESOLVER = caching.compose(fromLocalMaven).compose(fromRemote).apply(null);

        if (Files.exists(PATH_CACHED_RESOLVER)) {
            try (Reader reader = Files.newBufferedReader(PATH_CACHED_RESOLVER)) {
                CACHED_RESOLVER.deserialize(reader);
            }
        }

        Config.RESOLVER = CACHED_RESOLVER;
    }

    public static void teardown() throws IOException {
        try (Writer writer = Files.newBufferedWriter(PATH_CACHED_RESOLVER, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            CACHED_RESOLVER.serialize(writer);
        }
    }

    public static void printObj() throws IOException {
        InstallProfile profile = Jackson.JSON.readValue(new File("src/test/resources/install_profile_1.12.2.json"), InstallProfile.class);

        profile.validate();

        Pair<ObjectNode, ObjectNode> modified = profile.convert(Jackson.JSON.getNodeFactory());

        System.out.println("obj install_profile.json");
        System.out.println(Jackson.write(modified.left()));
        System.out.println("obj version.json");
        System.out.println(Jackson.write(modified.right()));
    }
}
